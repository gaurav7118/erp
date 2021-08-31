/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.spring.mrp.labormanagement;

import com.krawler.common.admin.AuditAction;
import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.common.util.Constants;
import com.krawler.common.util.StringUtil;
import com.krawler.hql.accounting.AccountingException;
import com.krawler.hql.accounting.StaticValues;
import com.krawler.spring.accounting.companypreferances.accCompanyPreferencesDAO;
import com.krawler.spring.accounting.handler.AccountingHandlerDAO;
import com.krawler.spring.auditTrailModule.auditTrailDAO;
import com.krawler.spring.authHandler.authHandler;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.spring.common.fieldDataManager;
import com.krawler.spring.exportFunctionality.exportMPXDAOImpl;
import com.krawler.spring.mrp.machinemanagement.accMachineManagementController;
import com.krawler.spring.sessionHandler.sessionHandlerImpl;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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
public class AccLabourControllerCMN extends MultiActionController implements MessageSourceAware {

    private HibernateTransactionManager txnManager;
    private MessageSource messageSource;
    private auditTrailDAO auditTrailObj;
    private accLabourDAO accLabourDAO;
    private AccLabourServiceDAO accLabourServiceDAO;
    private exportMPXDAOImpl exportDaoObj;
    private fieldDataManager fieldDataManagercntrl;
    private AccountingHandlerDAO accountingHandlerDAOobj;
    private accCompanyPreferencesDAO accCompanyPreferencesObj;

    public void setAccLabourServiceDAO(AccLabourServiceDAO accLabourServiceDAO) {
        this.accLabourServiceDAO = accLabourServiceDAO;
    }

    public void setAccLabourDAO(accLabourDAO accLabourDAO) {
        this.accLabourDAO = accLabourDAO;
    }

    @Override
    public void setMessageSource(MessageSource ms) {
        this.messageSource = ms;

    }

    public void setauditTrailDAO(auditTrailDAO auditTrailDAOObj) {
        this.auditTrailObj = auditTrailDAOObj;
    }

    public void setTxnManager(HibernateTransactionManager txnManager) {
        this.txnManager = txnManager;
    }

    public void setexportMPXDAOImpl(exportMPXDAOImpl exportDaoObj) {
        this.exportDaoObj = exportDaoObj;
    }

    public void setFieldDataManager(fieldDataManager fieldDataManagercntrl) {
        this.fieldDataManagercntrl = fieldDataManagercntrl;
    }

    public void setaccountingHandlerDAO(AccountingHandlerDAO accountingHandlerDAOobj) {
        this.accountingHandlerDAOobj = accountingHandlerDAOobj;
    }

    public void setaccCompanyPreferencesDAO(accCompanyPreferencesDAO accCompanyPreferencesObj) {
        this.accCompanyPreferencesObj = accCompanyPreferencesObj;
    }

    public ModelAndView saveLabourInformation(HttpServletRequest request, HttpServletResponse response) throws SessionExpiredException, ParseException {
        JSONObject jobj = new JSONObject();
        String billid = "";
        String billno = "";
        boolean issuccess = false;
        String msg = "";
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("RIC_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        try {
            /*
             Save Labours
             */
            HashMap<String, Object> requestParams = getLabourInformationMap(request);
            JSONObject jSONObject = accLabourServiceDAO.saveLabour(requestParams);
            msg = jSONObject.optString("msg");
            txnManager.commit(status);
            if (jSONObject.has("labour") && (jSONObject.opt("labour")) != null) {
                Map<String, Object> syncParams = new HashMap<String, Object>();
                syncParams.put("companyId", sessionHandlerImpl.getCompanyid(request));
                syncParams.put("userId", sessionHandlerImpl.getUserid(request));
                syncParams.put("isAutoSync", true);
                syncParams.put("labour", jSONObject.opt("labour"));
                jobj = accLabourServiceDAO.syncLabour(syncParams);
                JSONArray labourIdsArray = jobj.optJSONArray("ids");
                if (labourIdsArray != null && labourIdsArray.length() > 0) {
                    Map<String, Object> updatemap = new HashMap();
                    updatemap.put("ids", labourIdsArray);
                    jSONObject = accLabourServiceDAO.updateLabourFlag(updatemap);
                } else {
                    msg = msg + " "+messageSource.getMessage("acc.mrp.resource.notsync", null, RequestContextUtils.getLocale(request));
                }
            }
            issuccess = true;
        } catch (Exception ex) {
            if (status != null) {
                txnManager.rollback(status);
                msg = "" + ex.getMessage();
            }
            Logger.getLogger(AccLabourControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(AccLabourControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public ModelAndView saveResourceCost(HttpServletRequest request, HttpServletResponse response) throws SessionExpiredException, ParseException {
        JSONObject jobj = new JSONObject();
        String billid = "";
        String billno = "";
        boolean issuccess = false;
        String msg = "";
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("RIC_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        try {
            /*
             Save Labours cost
             */
            Map<String, Object> requestParams = getLabourCostMap(request);
            JSONObject jSONObject = accLabourServiceDAO.saveLabourCost(requestParams);
            msg = jSONObject.optString("msg");
            txnManager.commit(status);
            issuccess = true;
        } catch (Exception ex) {
            if (status != null) {
                txnManager.rollback(status);
                msg = "" + ex.getMessage();
            }
            Logger.getLogger(AccLabourControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(AccLabourControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public Map<String, Object> getLabourCostMap(HttpServletRequest request) throws SessionExpiredException, ParseException {
        Map<String, Object> requestParams = new HashMap<String, Object>();
        Date effectivedate = !StringUtil.isNullOrEmpty(request.getParameter("effectivedate")) ? authHandler.getDateOnlyFormat().parse(request.getParameter("effectivedate")) : null;
        requestParams.put("labourId", request.getParameter("labourId"));
        requestParams.put("effectivedate", effectivedate);
        if (!StringUtil.isNullOrEmpty(request.getParameter("resourceCostId"))) {
            requestParams.put("resourceCostId", request.getParameter("resourceCostId"));
        }
        requestParams.put("resourcecost", request.getParameter("resourcecost"));
        requestParams.put("company", sessionHandlerImpl.getCompanyid(request));
        requestParams.put("request", request);
        return requestParams;
    }

    public HashMap<String, Object> getLabourInformationMap(HttpServletRequest request) throws ServiceException, AccountingException {
        List list = new ArrayList();
        HashMap<String, Object> requestParams = new HashMap<String, Object>();
        requestParams.put("request", request);
        boolean isEdit = false;
        try {
            String sequenceformat = request.getParameter("sequenceformat") != null ? request.getParameter("sequenceformat") : "NA";
            String companyId = sessionHandlerImpl.getCompanyid(request);
            Date dob = !StringUtil.isNullOrEmpty(request.getParameter("dob")) ? authHandler.getDateOnlyFormat().parse(request.getParameter("dob")) : null;
            Date prdate = !StringUtil.isNullOrEmpty(request.getParameter("prdate")) ? authHandler.getDateOnlyFormat().parse(request.getParameter("prdate")) : null;
            Date expirydatepassport = !StringUtil.isNullOrEmpty(request.getParameter("expirydatepassport")) ? authHandler.getDateOnlyFormat().parse(request.getParameter("expirydatepassport")) : null;
            if (dob != null) {
                requestParams.put("dob", dob);
            }
            if (expirydatepassport != null) {
                requestParams.put("expirydatepassport", expirydatepassport);
            }
            if (prdate != null) {
                requestParams.put("prdate", prdate);
            }
            requestParams.put("empcode", request.getParameter("empcode"));
            requestParams.put("sequenceformat", request.getParameter("sequenceformat"));
            requestParams.put("fname", request.getParameter("fname"));
            requestParams.put("lname", request.getParameter("lname"));
            requestParams.put("mname", request.getParameter("mname"));
            requestParams.put("age", request.getParameter("age"));
            requestParams.put("gender", request.getParameter("gender"));
            requestParams.put("maritalstatus", request.getParameter("maritalstatus"));
            requestParams.put("bgroup", request.getParameter("bgroup"));
            requestParams.put("nationality", request.getParameter("nationality"));
            requestParams.put("countryorigin", request.getParameter("countryorigin"));
            requestParams.put("department", request.getParameter("department"));
            requestParams.put("dlicenseno", request.getParameter("dlicenseno"));
            requestParams.put("passportno", request.getParameter("passportno"));
            requestParams.put("paycycle", request.getParameter("paycycle"));
            requestParams.put("residentstatus", request.getParameter("residentstatus"));
            requestParams.put("race", request.getParameter("race"));
            requestParams.put("religion", request.getParameter("religion"));
            requestParams.put("bankac", request.getParameter("bankac"));
            requestParams.put("bankaname", request.getParameter("bankaname"));
            requestParams.put("accountname", request.getParameter("accountname"));
            requestParams.put("accountnumber", request.getParameter("accountnumber"));
            requestParams.put("banknumber", request.getParameter("banknumber"));
            requestParams.put("bankbranch", request.getParameter("bankbranch"));
            requestParams.put("branchnumber", request.getParameter("branchnumber"));
            requestParams.put("paymentmethod", !StringUtil.isNullOrEmpty(request.getParameter("paymentmethod")) ? request.getParameter("paymentmethod") : null);
            if (!StringUtil.isNullOrEmpty(request.getParameter("keyskill"))) {
                requestParams.put("keyskill", request.getParameter("keyskill"));
            }
            requestParams.put("shifttiming", request.getParameter("shifttiming"));
            requestParams.put("company", sessionHandlerImpl.getCompanyid(request));
            requestParams.put("createdby", sessionHandlerImpl.getUserid(request));
            requestParams.put("modifiedby", sessionHandlerImpl.getUserid(request));
            String workCentre = request.getParameter("workcentre");
            KwlReturnObject result = null;
            requestParams.put("workcentre", workCentre);
            String customfield = request.getParameter("customfield");
            requestParams.put("customfield", customfield);
            String labourId = request.getParameter("billid");
            requestParams.put("labourId", labourId);
            String entryNumber = request.getParameter("empcode");
            int countduplicate = 0;
            HashMap<String, Object> map = new HashMap<String, Object>();
            map.put("companyId", companyId);
            map.put("entryNumber", entryNumber);
            if (!StringUtil.isNullOrEmpty(labourId)) {
                map.put("labourId", labourId);
            }
            if (StringUtil.isNullOrEmpty(labourId) && sequenceformat.equals("NA")) {
                result = accLabourDAO.getLabourNumberCount(map);
                countduplicate = result.getRecordTotalCount();
                if (countduplicate > 0 && sequenceformat.equals("NA")) {
                    throw new AccountingException(messageSource.getMessage("acc.labour.empnumber", null, RequestContextUtils.getLocale(request)) + " <b>" + entryNumber + "</b> "  + messageSource.getMessage("acc.field.alreadyexists.", null, RequestContextUtils.getLocale(request)));
                }
            } else if (sequenceformat.equals("NA")) {
                map.put("billid", labourId);
                result = accLabourDAO.getLabourNumberCount(map);
                countduplicate = result.getRecordTotalCount();
                if (countduplicate > 0 && sequenceformat.equals("NA")) {
                    throw new AccountingException(messageSource.getMessage("acc.labour.empnumber", null, RequestContextUtils.getLocale(request)) + " <b>" + entryNumber + "</b> "  + messageSource.getMessage("acc.field.alreadyexists.", null, RequestContextUtils.getLocale(request)));
                }
            }
            Labour editLabourObject = null;
            if (!StringUtil.isNullOrEmpty(labourId)) {// for edit case
                KwlReturnObject receiptObj = accountingHandlerDAOobj.getObject(Labour.class.getName(), labourId);
                editLabourObject = (Labour) receiptObj.getEntityList().get(0);
            }
            synchronized (this) { //this block is used to generate auto sequence number if number is not duplicate
                String nextAutoNo = "";
                if (editLabourObject != null) {
                    if (sequenceformat.equals("NA")) {
                        if (!entryNumber.equals(editLabourObject.getEmpcode())) {
                            requestParams.put("empcode", entryNumber);
                            requestParams.put("autogenerated", entryNumber.equals(nextAutoNo));
                        }
                    }
                } else {
                    if (!sequenceformat.equals("NA")) {
                        Map<String, Object> seqNumberMap = new HashMap<String, Object>();
                        seqNumberMap = accCompanyPreferencesObj.getNextAutoNumber_Modified(companyId, StaticValues.AUTONUM_LABOUR, sequenceformat, false, null);// There is no craetion date hence sending date as null
                        requestParams.put("empcode", seqNumberMap.get(Constants.AUTO_ENTRYNUMBER));//complete number
                        requestParams.put(Constants.SEQNUMBER, seqNumberMap.get(Constants.SEQNUMBER));//interger part
                        requestParams.put(Constants.DATEPREFIX, seqNumberMap.get(Constants.DATEPREFIX));
                        requestParams.put(Constants.DATEAFTERPREFIX, seqNumberMap.get(Constants.DATEAFTERPREFIX));
                        requestParams.put(Constants.DATESUFFIX, seqNumberMap.get(Constants.DATESUFFIX));
                        requestParams.put(Constants.SEQFORMAT, sequenceformat);
                    }
                    if (sequenceformat.equals("NA")) {
                        requestParams.put("empcode", entryNumber);
                    }
                    requestParams.put("autogenerated", sequenceformat.equals("NA") ? false : true);
                }
            }
            if (sequenceformat.equals("NA")) {//In case of NA checks wheather this number can also be generated by a sequence format or not
                List resultList = accCompanyPreferencesObj.checksEntryNumberForSequenceNumber(Constants.Labour_Master, entryNumber, companyId);
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
        } catch (ParseException ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
        return requestParams;
    }

    public ModelAndView getLaboursMerge(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean success = false;
        try {
            /*
             Below function is used to create column model for grid
             */
            HashMap<String, Object> requestParams = getLabourMasterCommonParameters(request);
            requestParams.put("request", request);
            requestParams.put("start", request.getParameter("start"));
            requestParams.put("limit", request.getParameter("limit"));
            requestParams.put("ss", request.getParameter("ss"));
            String moduleId = request.getParameter("moduleid");
            String searchJson = request.getParameter("searchJson");
            String filterConjuction = request.getParameter("filterConjuctionCriteria");
            if (!StringUtil.isNullOrEmpty(searchJson) && !StringUtil.isNullOrEmpty(filterConjuction)) {
                requestParams.put("searchJson", searchJson);
                requestParams.put("filterConjuctionCriteria", filterConjuction);
            }
            if (!StringUtil.isNullOrEmpty(moduleId)) {
                requestParams.put("moduleid", moduleId);
            }
            accLabourServiceDAO.getColumnModelAndRecordDataForLabours(requestParams, jobj);
            success = true;
        } catch (Exception ex) {
            msg = ex.getMessage();
        } finally {
            try {
                jobj.put(Constants.RES_success, success);
                jobj.put(Constants.RES_msg, msg == null ? "null" : msg);
            } catch (JSONException ex) {
                Logger.getLogger(AccLabourControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView(Constants.jsonView, Constants.model, jobj.toString());
    }

    public ModelAndView ExportLabours(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        JSONObject jobj1 = new JSONObject();
        JSONArray DataJArr = new JSONArray();
        List resultList = new ArrayList();
        boolean isSummaryReport = false;
        String view = "jsonView_ex";
        String msg = "";
        try {
            HashMap<String, Object> requestParams = getLabourMasterCommonParameters(request);
            requestParams.put("isExport", true);
            requestParams.put("request", request);
            accLabourServiceDAO.getColumnModelAndRecordDataForLabours(requestParams, jobj1);
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
            Logger.getLogger(AccLabourControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(AccLabourControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public ModelAndView getAssignTaskList(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean success = false;
        try {
            /*
             Below function is used to create column model for grid
             */
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            requestParams.put("request", request);
            requestParams.put("start", request.getParameter("start"));
            requestParams.put("limit", request.getParameter("limit"));
            accLabourServiceDAO.getColumnModelForAssignTaskList(requestParams, jobj);
            success = true;
        } catch (Exception ex) {
            msg = ex.getMessage();
        } finally {
            try {
                jobj.put(Constants.RES_success, success);
                jobj.put(Constants.RES_msg, msg == null ? "null" : msg);
            } catch (JSONException ex) {
                Logger.getLogger(AccLabourControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView(Constants.jsonView, Constants.model, jobj.toString());
    }

    public ModelAndView getResourceList(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean success = false;
        try {
            /*
             Below function is used to create column model for grid
             */
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            requestParams.put("request", request);
            requestParams.put("start", request.getParameter("start"));
            requestParams.put("limit", request.getParameter("limit"));
            requestParams.put("ss", request.getParameter("ss"));
            accLabourServiceDAO.getColumnModelForResourceList(requestParams, jobj);
            success = true;
        } catch (Exception ex) {
            msg = ex.getMessage();
        } finally {
            try {
                jobj.put(Constants.RES_success, success);
                jobj.put(Constants.RES_msg, msg == null ? "null" : msg);
            } catch (JSONException ex) {
                Logger.getLogger(AccLabourControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView(Constants.jsonView, Constants.model, jobj.toString());
    }

    public ModelAndView getResourceCostList(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean success = false;
        String start = "", limit = "";
        JSONArray pagedJson = new JSONArray();
        JSONArray jArray = new JSONArray();
        try {
            Map<String, Object> requestParams = new HashMap<String, Object>();
            requestParams.put("request", request);
            DateFormat userdf = authHandler.getUserDateFormatterWithoutTimeZone(request); //TimeZone Problem is occured so that Method getUserDateFormatter()is replaced with Method getUserDateFormatterWithoutTimeZone().
            requestParams.put(Constants.userdf, userdf);
            requestParams.put("LabourId", request.getParameter("LabourId"));
            requestParams.put("start", request.getParameter("start"));
            requestParams.put("limit", request.getParameter("limit"));
            /*
             Below function is used to create column model for grid
             */
            accLabourServiceDAO.getResourceCostList(requestParams, jobj);
            success = true;
        } catch (Exception ex) {
            msg = ex.getMessage();
        } finally {
            try {
                jobj.put(Constants.RES_success, success);
                jobj.put(Constants.RES_msg, msg == null ? "null" : msg);
            } catch (JSONException ex) {
                Logger.getLogger(AccLabourControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView(Constants.jsonView, Constants.model, jobj.toString());
    }

    /**
     *
     * @param request
     * @param object
     * @Description = put column model into JSON object
     */
    public void getColumnModelForResourceCostList(HttpServletRequest request, JSONObject object) {
        JSONObject jMeta = new JSONObject();
        JSONArray jarrColumns = new JSONArray();
        JSONArray jarrRecords = new JSONArray();
        JSONObject jobjTemp = new JSONObject();
        String storeRec = "";
        try {
            storeRec = "cost,effectivedate";
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
            jobjTemp.put("dataIndex", "cost");
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
            object.put("columns", jarrColumns);
            jMeta.put("totalProperty", "totalCount");
            jMeta.put("root", "coldata");
            jMeta.put("fields", jarrRecords);
            object.put("metaData", jMeta);

        } catch (JSONException ex) {
            Logger.getLogger(AccLabourControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public ModelAndView getResourceAnalysisColumnModel(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String view = "jsonView_ex";
        try {
            boolean isExport = Boolean.parseBoolean(request.getParameter("isExport"));
            jobj = getResourceAnalysisColumnModel(request, isExport);
            String fileType = request.getParameter("filetype");
            if (StringUtil.equal(fileType, "print")) {
                String GenerateDate = authHandler.getDateOnlyFormat(request).format(new Date());
                jobj.put("GenerateDate", GenerateDate);
                view = "jsonView-empty";
            }
        } catch (Exception ex) {
            Logger.getLogger(AccLabourControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new ModelAndView(view, "model", jobj.toString());
    }

    public JSONObject getResourceAnalysisColumnModel(HttpServletRequest request, boolean isExport) {
        JSONObject jobj = new JSONObject();
        JSONArray dataJArr = new JSONArray();
        try {
            // Column Model
            JSONObject commData = new JSONObject();
            JSONObject jMeta = new JSONObject();
            JSONArray jarrColumns = new JSONArray();
            JSONArray jarrRecords = new JSONArray();
            JSONObject jobjTemp = new JSONObject();

            String StoreRec = "id, taskname, resourcename, workordernumber, startdate, enddate, schedulework, status, routecode, workcenter, branch, plant";
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
            jobjTemp.put("header", messageSource.getMessage("acc.resourceanalysis.columns.taskname", null, RequestContextUtils.getLocale(request)));
            jobjTemp.put("dataIndex", "taskname");
            jobjTemp.put("align", "left");
            jobjTemp.put("width", 75);
            jobjTemp.put("pdfwidth", 75);
            jobjTemp.put("sortable", true);
            jarrColumns.put(jobjTemp);

            jobjTemp = new JSONObject();
            jobjTemp.put("header", messageSource.getMessage("acc.resourceanalysis.columns.resourcename", null, RequestContextUtils.getLocale(request)));
            jobjTemp.put("dataIndex", "resourcename");
            jobjTemp.put("align", "left");
            jobjTemp.put("width", 75);
            jobjTemp.put("pdfwidth", 75);
            jobjTemp.put("sortable", true);
            jarrColumns.put(jobjTemp);

            jobjTemp = new JSONObject();
            jobjTemp.put("header", messageSource.getMessage("acc.resourceanalysis.columns.workordernumber", null, RequestContextUtils.getLocale(request)));
            jobjTemp.put("dataIndex", "workordernumber");
            jobjTemp.put("align", "left");
            jobjTemp.put("width", 75);
            jobjTemp.put("pdfwidth", 75);
            jobjTemp.put("sortable", true);
            jarrColumns.put(jobjTemp);

            jobjTemp = new JSONObject();
            jobjTemp.put("header", messageSource.getMessage("acc.resourceanalysis.columns.workordernumber", null, RequestContextUtils.getLocale(request)));
            jobjTemp.put("dataIndex", "workordernumber");
            jobjTemp.put("align", "left");
            jobjTemp.put("width", 75);
            jobjTemp.put("pdfwidth", 75);
            jobjTemp.put("sortable", true);
            jarrColumns.put(jobjTemp);

            jobjTemp = new JSONObject();
            jobjTemp.put("header", messageSource.getMessage("acc.resourceanalysis.columns.startdate", null, RequestContextUtils.getLocale(request)));
            jobjTemp.put("dataIndex", "startdate");
            jobjTemp.put("align", "center");
            jobjTemp.put("width", 75);
            jobjTemp.put("pdfwidth", 75);
            jobjTemp.put("sortable", true);
            jarrColumns.put(jobjTemp);

            jobjTemp = new JSONObject();
            jobjTemp.put("header", messageSource.getMessage("acc.resourceanalysis.columns.enddate", null, RequestContextUtils.getLocale(request)));
            jobjTemp.put("dataIndex", "enddate");
            jobjTemp.put("align", "center");
            jobjTemp.put("width", 75);
            jobjTemp.put("pdfwidth", 75);
            jobjTemp.put("sortable", true);
            jarrColumns.put(jobjTemp);

            jobjTemp = new JSONObject();
            jobjTemp.put("header", messageSource.getMessage("acc.resourceanalysis.columns.schedulework", null, RequestContextUtils.getLocale(request)));
            jobjTemp.put("dataIndex", "schedulework");
            jobjTemp.put("align", "right");
            jobjTemp.put("width", 75);
            jobjTemp.put("pdfwidth", 75);
            jobjTemp.put("sortable", true);
            jarrColumns.put(jobjTemp);

            jobjTemp = new JSONObject();
            jobjTemp.put("header", messageSource.getMessage("acc.resourceanalysis.columns.status", null, RequestContextUtils.getLocale(request)));
            jobjTemp.put("dataIndex", "status");
            jobjTemp.put("align", "center");
            jobjTemp.put("width", 75);
            jobjTemp.put("pdfwidth", 75);
            jobjTemp.put("sortable", true);
            jarrColumns.put(jobjTemp);

            jobjTemp = new JSONObject();
            jobjTemp.put("header", messageSource.getMessage("acc.resourceanalysis.columns.routecode", null, RequestContextUtils.getLocale(request)));
            jobjTemp.put("dataIndex", "routecode");
            jobjTemp.put("align", "left");
            jobjTemp.put("width", 75);
            jobjTemp.put("pdfwidth", 75);
            jobjTemp.put("sortable", true);
            jarrColumns.put(jobjTemp);

            jobjTemp = new JSONObject();
            jobjTemp.put("header", messageSource.getMessage("acc.resourceanalysis.columns.workcenter", null, RequestContextUtils.getLocale(request)));
            jobjTemp.put("dataIndex", "workcenter");
            jobjTemp.put("align", "center");
            jobjTemp.put("width", 75);
            jobjTemp.put("pdfwidth", 75);
            jobjTemp.put("sortable", true);
            jarrColumns.put(jobjTemp);

            jobjTemp = new JSONObject();
            jobjTemp.put("header", messageSource.getMessage("acc.resourceanalysis.columns.branch", null, RequestContextUtils.getLocale(request)));
            jobjTemp.put("dataIndex", "branch");
            jobjTemp.put("align", "center");
            jobjTemp.put("width", 75);
            jobjTemp.put("pdfwidth", 75);
            jobjTemp.put("sortable", true);
            jarrColumns.put(jobjTemp);

            jobjTemp = new JSONObject();
            jobjTemp.put("header", messageSource.getMessage("acc.resourceanalysis.columns.plant", null, RequestContextUtils.getLocale(request)));
            jobjTemp.put("dataIndex", "plant");
            jobjTemp.put("align", "left");
            jobjTemp.put("width", 75);
            jobjTemp.put("pdfwidth", 75);
            jobjTemp.put("sortable", true);
            jarrColumns.put(jobjTemp);

            commData.put("success", true);
            commData.put("coldata", dataJArr);
            commData.put("columns", jarrColumns);
            jMeta.put("totalProperty", "totalCount");
            jMeta.put("root", "coldata");
            commData.put("totalCount", 0);
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
            Logger.getLogger(AccLabourControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        }
        return jobj;
    }

    public ModelAndView getResolveConflictResourcesColumnModel(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean success = false;
        try {
            /*
             Below function is used to create column model for grid
             */
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            requestParams.put("request", request);
            requestParams.put("start", request.getParameter("start"));
            requestParams.put("limit", request.getParameter("limit"));
            accLabourServiceDAO.getResolveConflictResourcesColumnModel(requestParams, jobj);
            success = true;
        } catch (Exception ex) {
            msg = ex.getMessage();
        } finally {
            try {
                jobj.put(Constants.RES_success, success);
                jobj.put(Constants.RES_msg, msg == null ? "null" : msg);
            } catch (JSONException ex) {
                Logger.getLogger(AccLabourControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView(Constants.jsonView, Constants.model, jobj.toString());
    }

    public ModelAndView getResolveConflictTasksColumnModel(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String view = "jsonView_ex";
        try {
            boolean isExport = Boolean.parseBoolean(request.getParameter("isExport"));
            jobj = getResolveConflictTasksColumnModel(request, isExport);
            String fileType = request.getParameter("filetype");
            if (StringUtil.equal(fileType, "print")) {
                String GenerateDate = authHandler.getDateOnlyFormat(request).format(new Date());
                jobj.put("GenerateDate", GenerateDate);
                view = "jsonView-empty";
            }
        } catch (Exception ex) {
            Logger.getLogger(AccLabourControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new ModelAndView(view, "model", jobj.toString());
    }

    public JSONObject getResolveConflictTasksColumnModel(HttpServletRequest request, boolean isExport) {
        JSONObject jobj = new JSONObject();
        JSONArray dataJArr = new JSONArray();
        try {
            // Column Model
            JSONObject commData = new JSONObject();
            JSONObject jMeta = new JSONObject();
            JSONArray jarrColumns = new JSONArray();
            JSONArray jarrRecords = new JSONArray();
            JSONObject jobjTemp = new JSONObject();

            String StoreRec = "id, taskname, startdate, enddate";
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
            jobjTemp.put("header", messageSource.getMessage("acc.resourceanalysis.columns.taskname", null, RequestContextUtils.getLocale(request)));
            jobjTemp.put("dataIndex", "taskname");
            jobjTemp.put("align", "left");
            jobjTemp.put("width", 120);
            jobjTemp.put("pdfwidth", 120);
            jobjTemp.put("sortable", true);
            jarrColumns.put(jobjTemp);

            jobjTemp = new JSONObject();
            jobjTemp.put("header", messageSource.getMessage("acc.resolveconflict.columns.startdate", null, RequestContextUtils.getLocale(request)));
            jobjTemp.put("dataIndex", "startdate");
            jobjTemp.put("align", "left");
            jobjTemp.put("width", 120);
            jobjTemp.put("pdfwidth", 120);
            jobjTemp.put("sortable", true);
            jarrColumns.put(jobjTemp);

            jobjTemp = new JSONObject();
            jobjTemp.put("header", messageSource.getMessage("acc.resolveconflict.columns.enddate", null, RequestContextUtils.getLocale(request)));
            jobjTemp.put("dataIndex", "enddate");
            jobjTemp.put("align", "left");
            jobjTemp.put("width", 120);
            jobjTemp.put("pdfwidth", 120);
            jobjTemp.put("sortable", true);
            jarrColumns.put(jobjTemp);

            commData.put("success", true);
            commData.put("coldata", dataJArr);
            commData.put("columns", jarrColumns);
            jMeta.put("totalProperty", "totalCount");
            jMeta.put("root", "coldata");
            commData.put("totalCount", 0);
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
            Logger.getLogger(AccLabourControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        }
        return jobj;
    }

    public ModelAndView getSingleLabourToLoad(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        try {
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            String billid = request.getParameter("billid") != null ? request.getParameter("billid") : "";
            requestParams.put("billId", billid);
            DateFormat df = authHandler.getOnlyDateFormat(request);
            requestParams.put("df", df);
            KwlReturnObject result = null;
            requestParams.put("companyId", sessionHandlerImpl.getCompanyid(request));
            /*
             Get record for single labour
             */
            JSONObject labourObj = accLabourServiceDAO.getSingleLabourToLoad(requestParams);
            jobj.put("data", labourObj);
            issuccess = true;
        } catch (SessionExpiredException ex) {
            Logger.getLogger(AccLabourControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
            msg = ex.getMessage();
        } catch (Exception ex) {
            msg = "" + ex.getMessage();
            Logger.getLogger(AccLabourControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(AccLabourControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public ModelAndView deleteLabours(HttpServletRequest request, HttpServletResponse response) throws SessionExpiredException {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("JEC_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        try {
            /*
             Delete labour
             */
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            requestParams.put("request", request);
            
            requestParams.put("companyId", sessionHandlerImpl.getCompanyid(request));
            requestParams.put("cdomain", sessionHandlerImpl.getCompanySessionObj(request).getCdomain());
            requestParams.put("userId", sessionHandlerImpl.getUserid(request));
            requestParams.put("isPerm", Boolean.parseBoolean(request.getParameter("isPerm")));
            JSONObject jObject =accLabourServiceDAO.deleteLabours(requestParams);
            txnManager.commit(status);
            issuccess = true;
            if(jObject.has("msg") && !StringUtil.isNullOrEmpty(jObject.getString("msg"))){
                msg=jObject.getString("msg");
            }else{
            msg = messageSource.getMessage("acc.mrp.labour.delete", null, RequestContextUtils.getLocale(request));
            }
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = messageSource.getMessage("acc.labour.usedintransaction", null, RequestContextUtils.getLocale(request));
            Logger.getLogger(AccLabourControllerCMN.class.getName()).log(Level.SEVERE, "AccLabourControllerCMN.deleteLabours", ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(AccLabourControllerCMN.class.getName()).log(Level.SEVERE, "AccLabourControllerCMN.deleteLabours", ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    public ModelAndView deleteLaboursCost(HttpServletRequest request, HttpServletResponse response) throws SessionExpiredException {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("JEC_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        try {
            /*
             Delete labour cost
             */
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            requestParams.put("request", request);
            
            /*
            following parameters used for updating resource cost
            */
            requestParams.put("companyId", sessionHandlerImpl.getCompanyid(request));
            requestParams.put("cdomain", sessionHandlerImpl.getCompanySessionObj(request).getCdomain());
            requestParams.put("userId", sessionHandlerImpl.getUserid(request));
            /*
            above parameters used for updating resource cost
            */
            accLabourServiceDAO.deleteLaboursCost(requestParams);
            txnManager.commit(status);
            issuccess = true;
            msg = messageSource.getMessage("acc.mrp.labour.ResourceCost.deletecost", null, RequestContextUtils.getLocale(request));
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            Logger.getLogger(AccLabourControllerCMN.class.getName()).log(Level.SEVERE, "AccLabourControllerCMN.deleteLabours", ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(AccLabourControllerCMN.class.getName()).log(Level.SEVERE, "AccLabourControllerCMN.deleteLabours", ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    public ModelAndView getLabourForCombo(HttpServletRequest request, HttpServletResponse response) throws SessionExpiredException {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        try {
            JSONArray jSONArray = new JSONArray();
            Map<String, Object> hashMap = new HashMap<String, Object>();
            hashMap.put("companyId", sessionHandlerImpl.getCompanyid(request));
            if (request.getParameter("workcenterid") != null && !StringUtil.isNullOrEmpty(request.getParameter("workcenterid").toString())) {
                hashMap.put("workcenterid", request.getParameter("workcenterid").toString());
            }
            jSONArray = accLabourServiceDAO.getLabourCombo(hashMap);
            jobj.put("data", jSONArray);
            jobj.put("count", jSONArray.length());
            issuccess = true;

        } catch (JSONException ex) {
            Logger.getLogger(AccLabourControllerCMN.class.getName()).log(Level.SEVERE, "AccLabourControllerCMN.deleteLabours", ex);
        } catch (ServiceException ex) {
            Logger.getLogger(AccLabourControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(AccLabourControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", "");
            } catch (JSONException ex) {
                Logger.getLogger(AccLabourControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public ModelAndView syncLabourToPM(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        JSONObject jSONObject=null;
        String msg = "";
        boolean issuccess = false;
        try {
            /*
             Below function is used to sync labour
             */
            Map<String, Object> requestParams = new HashMap<String, Object>();
            requestParams.put("companyId", sessionHandlerImpl.getCompanyid(request));
            requestParams.put("userId", sessionHandlerImpl.getUserid(request));
            requestParams.put("syncable", false);
            jobj = accLabourServiceDAO.syncLabour(requestParams);
            
            JSONArray labourIdsArray = jobj.optJSONArray("ids");
            if (labourIdsArray !=null && labourIdsArray.length() > 0) {
                Map<String, Object> updatemap = new HashMap();
                updatemap.put("ids", labourIdsArray);
                jSONObject = accLabourServiceDAO.updateLabourFlag(updatemap);
            }
           if (jobj.has(Constants.RES_success)) {
                if (jobj.get(Constants.RES_success).toString().equalsIgnoreCase("false")) {
                    issuccess = false;
                } else {
                    issuccess = true;
                }
            }
            if (issuccess) {
                msg = messageSource.getMessage("acc.labourMaster.syncLabourToPMSucess", null, RequestContextUtils.getLocale(request));
            } else {
                msg = messageSource.getMessage("acc.machineMaster.syncMachineToPMFailure", null, RequestContextUtils.getLocale(request));
            }
        } catch (Exception ex) {
            msg = ex.getMessage();
        } finally {
            try {
                jobj.put(Constants.RES_success, issuccess);
                jobj.put(Constants.RES_msg, msg == null ? "Fail to Integrate with PM" : msg);
            } catch (JSONException ex) {
                Logger.getLogger(AccLabourControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView(Constants.jsonView, Constants.model, jobj.toString());
    }

    public ModelAndView syncLabourCostToPM(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean success = false;
        try {
            /*
             Below function is used to sync labour
             */
            Map<String, Object> requestParams = new HashMap<String, Object>();
            requestParams.put("companyId", sessionHandlerImpl.getCompanyid(request));
            requestParams.put("cdomain", sessionHandlerImpl.getCompanySessionObj(request).getCdomain());
            requestParams.put("userId", sessionHandlerImpl.getUserid(request));
            String labourId = request.getParameter("LabourId");
            if (!StringUtil.isNullOrEmpty(labourId)) {
                requestParams.put("labourId", labourId);
            }
            JSONObject jSONObject = accLabourServiceDAO.syncLabourCost(requestParams);
            if (jSONObject.has(Constants.RES_success)) {
                if (jSONObject.get(Constants.RES_success).toString().equalsIgnoreCase("false")) {
                    success = false;
                } else {
                    success = true;
                }
            }
            if (success) {
                msg = messageSource.getMessage("acc.labourMaster.syncLabourCostToPMSucess", null, RequestContextUtils.getLocale(request));
            } else {
                msg = messageSource.getMessage("acc.machineMaster.syncMachineToPMFailure", null, RequestContextUtils.getLocale(request));
            }
        } catch (Exception ex) {
            msg = ex.getMessage();
        } finally {
            try {
                jobj.put(Constants.RES_success, success);
                jobj.put(Constants.RES_msg, msg == null ? "null" : msg);
            } catch (JSONException ex) {
                Logger.getLogger(AccLabourControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView(Constants.jsonView, Constants.model, jobj.toString());
    }
    
    public ModelAndView exportLabourAllocationReport(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        try {
            String dateFormatPattern = "yyyy-MM-dd";
            String companyid = sessionHandlerImpl.getCompanyid(request);
            String userid = sessionHandlerImpl.getUserid(request);
            String fromDate = request.getParameter("fromdate");
            String toDate = request.getParameter("todate");
            String resourceids = request.getParameter("resourceids");
            Map<String, Object> requestParams = new HashMap<>();
//            companyid = "48a2f172-f930-40c4-87ca-618ae75c4528";
//            userid = "d08e14c8-0c9a-48bd-86e2-ea7358ef2d37";
//            resourceids = "d08e14c8-0c9a-48bd-86e2-ea7358ef2d37,402880c4549e104b01549e1ef147013d";
            requestParams.put("companyid", companyid);
            requestParams.put("userid", userid);
            requestParams.put("request", request);
            requestParams.put("response", response);
            requestParams.put("fromdate", fromDate);
            requestParams.put("todate", toDate);
            requestParams.put("dateformat", dateFormatPattern);
            requestParams.put("resourceids", resourceids);
            
            jobj = accLabourServiceDAO.exportLabourAllocationReportXlsx(requestParams);
            if (jobj.has(Constants.RES_success)) {
                if (jobj.optBoolean(Constants.RES_success)) {
                    issuccess = true;
                }
            }
            if (issuccess) {
                msg = messageSource.getMessage("acc.labourMaster.syncLabourCostToPMSucess", null, RequestContextUtils.getLocale(request));
            } else {
                msg = messageSource.getMessage("acc.labourMaster.syncLabourCostToPMSucess", null, RequestContextUtils.getLocale(request));
            }
           
        } catch (SessionExpiredException | ServiceException ex) {
            Logger.getLogger(accMachineManagementController.class.getName()).log(Level.SEVERE, null, ex);
            issuccess = false;
        } finally {
            try {
                jobj.put(Constants.RES_success, issuccess);
                jobj.put(Constants.RES_msg, msg == null ? "null" : msg);
            } catch (JSONException ex) {
                Logger.getLogger(AccLabourControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView(Constants.jsonView, Constants.model, jobj.toString());
    }
    
    public HashMap<String, Object> getLabourMasterCommonParameters(HttpServletRequest request) {
        HashMap<String, Object> requestParams = new HashMap<>();
        try {
            DateFormat userdf = authHandler.getUserDateFormatter(request);
            requestParams.put(Constants.userdf, userdf);
            if (!StringUtil.isNullOrEmpty(request.getParameter("wcid"))) {
                requestParams.put("wcid", request.getParameter("wcid"));
            }
            if (!StringUtil.isNullOrEmpty(request.getParameter("labourids"))) {
                requestParams.put("billId", request.getParameter("labourids"));
            }
        } catch (Exception ex) {
            Logger.getLogger(AccLabourControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        }
        return requestParams;
    }
 
}
