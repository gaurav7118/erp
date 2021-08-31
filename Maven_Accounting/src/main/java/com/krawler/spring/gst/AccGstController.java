/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.spring.gst;

import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.common.util.Constants;
import com.krawler.common.util.StringUtil;
import com.krawler.common.util.URLUtil;
import com.krawler.esp.handlers.StorageHandler;
import com.krawler.common.admin.AuditAction;
import com.krawler.hql.accounting.AccountingException;
import com.krawler.hql.accounting.MultiEntityMapping;
import com.krawler.hql.accounting.CompanyAccountPreferences;
import com.krawler.hql.accounting.GstFormGenerationHistory;
import com.krawler.spring.accounting.account.accAccountDAO;
import com.krawler.spring.accounting.goodsreceipt.accGoodsReceiptControllerCMN;
import com.krawler.spring.accounting.handler.AccountingHandlerDAO;
import com.krawler.spring.accounting.invoice.AccInvoiceServiceHandler;
import com.krawler.spring.accounting.reports.AccReportsService;
import com.krawler.spring.auditTrailModule.auditTrailDAO;
import com.krawler.spring.authHandler.authHandler;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.spring.common.kwlCommonTablesDAO;
import com.krawler.spring.sessionHandler.sessionHandlerImpl;
import com.krawler.spring.storageHandler.storageHandlerImpl;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceAware;
import org.springframework.context.NoSuchMessageException;
import org.springframework.orm.hibernate3.HibernateTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.ModelAndViewDefiningException;
import org.springframework.web.servlet.mvc.multiaction.MultiActionController;
import org.springframework.web.servlet.support.RequestContextUtils;
import org.w3c.dom.Document;

/**
 *
 * @author krawler
 */
public class AccGstController extends MultiActionController implements MessageSourceAware {

    private AccGstService accGstService;
    private MessageSource messageSource;
    private auditTrailDAO auditTrailObj;
    private AccGstDAO accGstDAO;
    private kwlCommonTablesDAO kwlCommonTablesDAOObj;
    private HibernateTransactionManager txnManager;
    private accAccountDAO accAccountDAOobj;
    private AccReportsService accReportsService;
    private AccountingHandlerDAO accountingHandlerDAOobj;

    public void setAccGstService(AccGstService accGstService) {
        this.accGstService = accGstService;
    }

    @Override
    public void setMessageSource(MessageSource ms) {
        this.messageSource = ms;
    }

    public void setauditTrailDAO(auditTrailDAO auditTrailDAOObj) {
        this.auditTrailObj = auditTrailDAOObj;
    }

    public void setAccGstDAO(AccGstDAO accGstDAO) {
        this.accGstDAO = accGstDAO;
    }

    public void setKwlCommonTablesDAO(kwlCommonTablesDAO KwlCommonTablesDAOObj) {
        this.kwlCommonTablesDAOObj = KwlCommonTablesDAOObj;
    }

    public void setTxnManager(HibernateTransactionManager txnManager) {
        this.txnManager = txnManager;
    }
    
    public void setaccAccountDAO(accAccountDAO accAccountDAOobj) {
        this.accAccountDAOobj = accAccountDAOobj;
    }
    public void setAccReportsService(AccReportsService accReportsService) {
        this.accReportsService = accReportsService;
    }
    public void setaccountingHandlerDAO(AccountingHandlerDAO AccountingHandlerDAOobj) {
        this.accountingHandlerDAOobj = AccountingHandlerDAOobj;
    }
    
    public Map getGSTFormGenerationHistoryMap(HttpServletRequest request) {
        Map<String, Object> map = new HashMap();
        try {
            map.put("userid", request.getParameter("userid"));
            map.put("startdate", request.getParameter("startdate"));
            map.put("enddate", request.getParameter("enddate"));
            map.put("generationdate", request.getParameter("generationdate"));
            map.put(Constants.companyKey, sessionHandlerImpl.getCompanyid(request));
            map.put(Constants.df, authHandler.getDateOnlyFormat(request));
            map.put(Constants.start, request.getParameter(Constants.start));
            map.put(Constants.limit,request.getParameter(Constants.limit));
            map.put("searchForMaxStartDate", request.getParameter("searchForMaxStartDate"));
            map.put("searchForMaxEndDate", request.getParameter("searchForMaxEndDate"));
            map.put(Constants.isMultiEntity, request.getParameter(Constants.isMultiEntity));
        } catch (Exception ex) {
            Logger.getLogger(AccGstController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return map;
    }

    public ModelAndView getGSTFormGenerationHistory(HttpServletRequest request, HttpServletResponse response) throws JSONException {
        JSONObject jobj = new JSONObject();
        try {
            Map<String, Object> map = getGSTFormGenerationHistoryMap(request);
            JSONObject config = accGstService.getGSTFormGenerationHistoryConfig(map);
            JSONArray columns = new JSONArray();
            JSONArray records = new JSONArray();
            if (config.has("records") && config.get("records") != null) {
                records = config.getJSONArray("records");
            }
            if (config.has("columns") && config.get("columns") != null) {
                columns = config.getJSONArray("columns");
            }
            JSONObject dataJSON = accGstService.getGSTFormGenerationHistoryData(map);
            JSONArray data = new JSONArray();
            if (dataJSON.has("data") && dataJSON.get("data") != null) {
                data = dataJSON.getJSONArray("data");
            }
            int count = dataJSON.optInt("count", 0);
            JSONObject commData = new JSONObject();
            JSONObject jMeta = new JSONObject();
            commData.put("success", true);
            commData.put("coldata", data);
            commData.put("columns", columns);
            jMeta.put("totalProperty", "totalCount");
            jMeta.put("root", "coldata");
            commData.put("totalCount", count);
            jMeta.put("fields", records);
            commData.put("metaData", jMeta);
            JSONArray jcom = new JSONArray();
            jcom.put(commData);
            jobj.put("valid", true);
            jobj.put("data", commData);
        } catch (Exception ex) {
            Logger.getLogger(AccGstController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new ModelAndView("jsonView_ex", "model", jobj.toString());
    }

    public ModelAndView downloadSubmissionFile(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        try {
            Map<String, Object> map = getGSTFormGenerationHistoryMap(request);
            DateFormat df = (DateFormat) map.get(Constants.df);
            String id = request.getParameter("id");
            String destinationDirectory = storageHandlerImpl.GetDocStorePath();
            if (!StringUtil.isNullOrEmpty(id)) {
                KwlReturnObject result = kwlCommonTablesDAOObj.getObject(GstFormGenerationHistory.class.getName(), id);
                GstFormGenerationHistory gstFormGenerationHistory = (GstFormGenerationHistory) result.getEntityList().get(0);
                if (gstFormGenerationHistory != null) {
                    String fileName = gstFormGenerationHistory.getFileName();
                    File directory = new File(destinationDirectory +  Constants.GST_SUBMISSIONFILE_STORAGE_PATH);
                    if (directory.exists()) {
                        File file = new File(destinationDirectory+  Constants.GST_SUBMISSIONFILE_STORAGE_PATH + File.separator + fileName);
                        if (file != null && file.exists()) {
                            byte[] buff = new byte[(int) file.length()];
                            String type="pdf";
                            try {
                                FileInputStream fis = new FileInputStream(file);
                                int read = fis.read(buff);
                            } catch (IOException ex) {
                                fileName = "file_not_found.txt";
                            }
                            response.setHeader("Content-Disposition", "attachment; filename=\"" + ("Submission_" + df.format(gstFormGenerationHistory.getGenerationDate())) + "." + type + "\"");
                            response.setContentType("application/octet-stream");
                            response.setContentLength(buff.length);
                            response.getOutputStream().write(buff);
                            response.getOutputStream().flush();
                        }
                    }
                }
            }
        } catch (Exception e) {
            Logger.getLogger(AccGstController.class.getName()).log(Level.SEVERE, null, e);
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    
    public ModelAndView isForm03IsGenerated(HttpServletRequest request, HttpServletResponse response) throws JSONException {
        JSONObject jobj = new JSONObject();
        boolean isFileGenerated = false;
        boolean isSuccess = false;
        try {
            Map map = getGSTFormGenerationHistoryMap(request);
            isFileGenerated = isFileGenerated(map);
            isSuccess = true;
        } catch (Exception ex) {
            Logger.getLogger(AccGstController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            jobj.put("success", isSuccess);
            jobj.put("isFileGenerated", isFileGenerated);
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public boolean isFileGenerated(Map<String, Object> map) throws ServiceException {
        boolean isFileGenerated = false;
        KwlReturnObject result = accGstDAO.getGstFormGenerationHistory(map);
        List list = result.getEntityList();
        if (list != null && !list.isEmpty()) {
            isFileGenerated = true;
        }
        return isFileGenerated;
    }
    
    public ModelAndView getValidDateRangeForFileGeneration(HttpServletRequest request, HttpServletResponse response) throws JSONException {
        JSONObject jobj = new JSONObject();
        boolean isSuccess = false;
        try {
            
            Map<String,Object> requestParams = new HashMap<>();
            requestParams.put(Constants.companyid, sessionHandlerImpl.getCompanyid(request));
            requestParams.put(Constants.df, authHandler.getDateOnlyFormat(request));
            
            if(!StringUtil.isNullOrEmpty(request.getParameter(Constants.multiEntityId))){
                requestParams.put(Constants.multiEntityId, request.getParameter(Constants.multiEntityId));
            }
            if(!StringUtil.isNullOrEmpty(request.getParameter(Constants.multiEntityValue))){
                requestParams.put(Constants.multiEntityValue, request.getParameter(Constants.multiEntityValue));
            }
            jobj = accGstService.getValidDateRangeForFileGeneration(requestParams);
            isSuccess = true;
        } catch (Exception ex) {
            Logger.getLogger(AccGstController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            jobj.put("success", isSuccess);
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    
    public ModelAndView checkForDueInvoicesAndDOs(HttpServletRequest request, HttpServletResponse response) throws JSONException {
        JSONObject jobj = new JSONObject();
        boolean isSuccess = false;
        try {
            jobj = checkForDueInvoicesAndDOs(request);
            isSuccess = true;
        } catch (Exception ex) {
            Logger.getLogger(AccGstController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            jobj.put("success", isSuccess);
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    
    public JSONObject checkForDueInvoicesAndDOs(HttpServletRequest request) throws SessionExpiredException, UnsupportedEncodingException{
        JSONObject jObj = new JSONObject();
        try {
            
            HashMap<String, Object> requestParams = AccInvoiceServiceHandler.getInvoiceRequestMap(request);//To get Sales/Customer invoice Request parameter

            requestParams.put("isBadDebtInvoices", true);
            requestParams.put("baddebttype", 0);
            requestParams.put("badDebtCriteria", 1);
            requestParams.put("badDebtCalculationDate", request.getParameter("badDebtCalculationDate"));
            
            String searchJson = request.getParameter(Constants.Acc_Search_Json);
            String filterConjuctionCriteria = request.getParameter(Constants.Filter_Criteria);
            
            if (!StringUtil.isNullOrEmpty(searchJson) && !StringUtil.isNullOrEmpty(filterConjuctionCriteria)) {
                requestParams.put(Constants.moduleid, Constants.Acc_Invoice_ModuleId);
                requestParams.put(Constants.Acc_Search_Json, accReportsService.getSearchJsonByModule(requestParams));//To get search json for Sales/Customer invoice
            }
            
            boolean salesInvoiceExists=false;
            salesInvoiceExists = accGstService.checkForClaimableSalesInvoices(requestParams);
            
            requestParams = new HashMap<>();
            requestParams = accGoodsReceiptControllerCMN.getGoodsReceiptMap(request);//To get Purchase/Vendor invoice Request parameter
            if (!StringUtil.isNullOrEmpty(searchJson) && !StringUtil.isNullOrEmpty(filterConjuctionCriteria)) {
                requestParams.put(Constants.moduleid, Constants.Acc_Vendor_Invoice_ModuleId);
                requestParams.put(Constants.Acc_Search_Json, accReportsService.getSearchJsonByModule(requestParams));//To get search json for purchase/Vendor invoice
            }

            requestParams.put("isBadDebtInvoices", true);
            requestParams.put("baddebttype", 0);
            requestParams.put("badDebtCriteria", 1);
            requestParams.put("badDebtCalculationDate", request.getParameter("badDebtCalculationDate"));
            boolean purchaseInvoiceExists=false;
            purchaseInvoiceExists = accGstService.checkForClaimablePurchaseInvoices(requestParams);
            
            
            boolean doExists=false;
            Map mapForDo = getGSTFormGenerationHistoryMap(request);
            
            if (!StringUtil.isNullOrEmpty(searchJson) && !StringUtil.isNullOrEmpty(filterConjuctionCriteria)) {
                mapForDo.put(Constants.Acc_Search_Json, searchJson);
                mapForDo.put(Constants.Filter_Criteria, filterConjuctionCriteria);
                mapForDo.put(Constants.moduleid, Constants.Acc_Delivery_Order_ModuleId);
                mapForDo.put(Constants.Acc_Search_Json, accReportsService.getSearchJsonByModule((HashMap<String, Object>) mapForDo));//To get search json for DO
            }
            
            doExists = accGstService.checkForUnInvoicedDOs(mapForDo);
            jObj.put("purchaseInvoiceExists", purchaseInvoiceExists);
            jObj.put("salesInvoiceExists", salesInvoiceExists);
            jObj.put("doExists", doExists);
        } catch (JSONException ex) {
            Logger.getLogger(AccGstController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return jObj;
    }
    
    /**
     *
     * @param request
     * @param response
     * @return save entity mapping details from CP
     * @throws JSONException
     */
    public ModelAndView saveEntityMapping(HttpServletRequest request, HttpServletResponse response) throws JSONException {
        JSONObject jobj = new JSONObject();
        boolean isSuccess = false, isEdit = false;
        String msg = "";
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("WC_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        try {
            KwlReturnObject result = null;
            Map<String, Object> requestParams = new HashMap();
            requestParams.put("data", request.getParameter("data"));
            
            if (!StringUtil.isNullOrEmpty(request.getParameter(Constants.isEdit))) {
                isEdit = Boolean.parseBoolean(request.getParameter(Constants.isEdit));
                requestParams.put(Constants.isEdit, isEdit);
            }
            requestParams.put(Constants.companyid, sessionHandlerImpl.getCompanyid(request));
            result = accGstService.saveEntityMapping(requestParams);            
            String auditUpdatedRecList = "";
            String entityName = "";
            if (result != null) {
                if (isEdit) {
                    auditUpdatedRecList = result.getRecordTotalCount() > 1 && !result.getEntityList().get(1).equals("") ? " as" + (String) result.getEntityList().get(1) : ".";
                }
                entityName = result.getRecordTotalCount() > 2 ? (String) result.getEntityList().get(2) : "";
            }
                        
            txnManager.commit(status);
            String auditAction = isEdit ? " updated " : " added ";
            auditTrailObj.insertAuditLog(AuditAction.GST_DETAIL_ADD_UPDATE, "User " + sessionHandlerImpl.getUserFullName(request) + " has " + auditAction + " GST details for entity " + entityName + "" + auditUpdatedRecList, request, "", "");
            isSuccess = true;
            if (isEdit) {
                msg = messageSource.getMessage("acc.gst.entity.update.success", null, RequestContextUtils.getLocale(request));
            } else {
                msg = messageSource.getMessage("acc.gst.entity.save.success", null, RequestContextUtils.getLocale(request));
            }
        } catch (SessionExpiredException | NoSuchMessageException | ServiceException ex) {
            msg = messageSource.getMessage("acc.common.msg1", null, RequestContextUtils.getLocale(request));
            Logger.getLogger(AccGstController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            jobj.put("success", isSuccess);
            jobj.put("msg", msg);
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    
    /**
     *
     * @param request
     * @param response
     * @return list entity Mapping details
     * @throws JSONException
     */
    public ModelAndView getEntityDetails(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean isSuccess = false;
        try {
            Map<String, Object> requestParams = new HashMap<>();
            requestParams.put(Constants.start, request.getParameter(Constants.start));
            requestParams.put(Constants.limit, request.getParameter(Constants.limit));
            requestParams.put(Constants.companyid, sessionHandlerImpl.getCompanyid(request));
            if(!StringUtil.isNullOrEmpty(request.getParameter(Constants.multiEntityId))){
                requestParams.put(Constants.multiEntityId, request.getParameter(Constants.multiEntityId));
            }
            if (!StringUtil.isNullOrEmpty(request.getParameter(Constants.multiEntityValue))) {
                requestParams.put(Constants.multiEntityValue, request.getParameter(Constants.multiEntityValue));
            }
            jobj = accGstService.getEntityDetails(requestParams);
            isSuccess = true;
        } catch (Exception ex) {
            Logger.getLogger(AccGstController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put(Constants.RES_success, isSuccess);
            } catch (JSONException ex) {
                Logger.getLogger(AccGstController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView(Constants.jsonView, Constants.model, jobj.toString());
    }
    
    /**
     *
     * @param request
     * @param response
     * @return list entity for combo
     */
    public ModelAndView getMultiEntityForCombo(HttpServletRequest request, HttpServletResponse response){
        JSONObject jobj = new JSONObject();
        try{
            Map <String,Object> requestParams = new HashMap<>();
            requestParams.put(Constants.companyid, sessionHandlerImpl.getCompanyid(request));
            jobj = accGstService.getMultiEntityForCombo(requestParams);
        }catch(SessionExpiredException | ServiceException ex){
            Logger.getLogger(AccGstController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new ModelAndView("jsonView","model",jobj.toString());
    }
    
    /**
     *
     * @param request
     * @param response
     * @return success message
     * @throws JSONException
     */
    public ModelAndView deleteEntityMapping(HttpServletRequest request,HttpServletResponse response) throws JSONException{
        JSONObject jObj = new JSONObject();
        boolean isSuccess = false;
        String msg = "";
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("JEC_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        try{
            Map<String, Object> requestParams = new HashMap();
            requestParams.put("data", request.getParameter("data"));
            requestParams.put(Constants.companyid, sessionHandlerImpl.getCompanyid(request));
            KwlReturnObject result = accGstService.deleteEntityMapping(requestParams);
            String entityName = result.getMsg();            
            txnManager.commit(status);
            String auditMsg = "User " + sessionHandlerImpl.getUserFullName(request) + " has deleted GST details for entity " + entityName;
            auditTrailObj.insertAuditLog(AuditAction.GST_DETAIL_DELETE, auditMsg, request, "", "");
            isSuccess = true;
            msg = messageSource.getMessage("acc.common.msg.delete.entitymap", null, RequestContextUtils.getLocale(request));
        }catch(SessionExpiredException | ServiceException ex){
            msg = messageSource.getMessage("acc.gst.warning.delete.entitymap", null, RequestContextUtils.getLocale(request));
            txnManager.rollback(status);
            Logger.getLogger(AccGstController.class.getName()).log(Level.SEVERE, null, ex);
        }finally{
            jObj.put("success", isSuccess);
            jObj.put("msg", msg);
        }
        return new ModelAndView("jsonView","model",jObj.toString());
    }
    
    /**
     * Inserting Custom Dimension in SI,PI,JE,MP,RP,DN,CN,DO for process multi entity from Company Preferences
     * @param request
     * @param response
     * @return
     */
    public ModelAndView insertMutiEntityDimensions(HttpServletRequest request,HttpServletResponse response) {
        try {
            HashMap<String, Object> requestParams = new HashMap<>();
            requestParams.put(Constants.isMultiEntity, request.getParameter(Constants.isMultiEntity));
            requestParams.put(Constants.companyid, sessionHandlerImpl.getCompanyid(request));
            String subdomain = URLUtil.getDomainName(request);
            requestParams.put("DefaultValue", subdomain);
            accAccountDAOobj.insertDefaultCustomeFields(requestParams);
        } catch (SessionExpiredException | ServiceException ex) {
            Logger.getLogger(AccGstController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new ModelAndView();
    }
    
    /**
     *
     * @param request
     * @param response
     * @return success message
     * @throws JSONException
     */
    
    public ModelAndView getLatestDateOfFileGeneration(HttpServletRequest request, HttpServletResponse response) throws JSONException {
        JSONObject jobj = new JSONObject();
        boolean isSuccess = false;
        try {
            
            Map<String,Object> requestParams = new HashMap<>();
            requestParams.put(Constants.companyid, sessionHandlerImpl.getCompanyid(request));
            requestParams.put(Constants.df, authHandler.getDateOnlyFormat(request));
            
            if(!StringUtil.isNullOrEmpty(request.getParameter(Constants.multiEntityId))){
                requestParams.put(Constants.multiEntityId, request.getParameter(Constants.multiEntityId));
            }
            requestParams.put("searchForMaxStartDate", true);
            jobj = accGstService.getLatestDateOfFileGeneration(requestParams);
            isSuccess = true;
        } catch (Exception ex) {
            Logger.getLogger(AccGstController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            jobj.put("success", isSuccess);
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    
    /*
     * Method for finding pending transactions in selected period.
     */ 
    public ModelAndView checkForPendingTransactions(HttpServletRequest request, HttpServletResponse response) throws JSONException{
        JSONObject jobj = new JSONObject();
        boolean isSuccess = false;
        try {
            HashMap<String,Object> requestParams = new HashMap<>();
            requestParams.put(Constants.companyid, sessionHandlerImpl.getCompanyid(request));
            requestParams.put(Constants.df, authHandler.getDateOnlyFormat(request));
            
            String startdate = request.getParameter(Constants.REQ_startdate);
            String enddate = request.getParameter(Constants.REQ_enddate);
            
            requestParams.put("startdate",startdate);
            requestParams.put("enddate",enddate);
            requestParams.put("excludeRejectedRecords","true");
            jobj = accGstService.checkForPendingTransactions(requestParams);
            isSuccess = true;
        } catch (Exception ex) {
            Logger.getLogger(AccGstController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            jobj.put("success", isSuccess);
        }
        return new ModelAndView("jsonView","model",jobj.toString());
    }
    
    public ModelAndView exportTXTGAFFile(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        try {
            JSONObject paramsJObj = StringUtil.convertRequestToJsonObject(request);
            String companyid = paramsJObj.optString(Constants.companyKey);
            java.io.ByteArrayOutputStream baos = new ByteArrayOutputStream();
            StringBuilder report = new StringBuilder();
            KwlReturnObject result = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), companyid);
            CompanyAccountPreferences companyAccountPreferences = (CompanyAccountPreferences) result.getEntityList().get(0);
            String gafVersion = companyAccountPreferences.getIafVersion();
            switch (gafVersion) {
                case Constants.GAFFileVersion_1:
                    report = accGstService.generateTXTGAFV1(paramsJObj);
                    break;
                case Constants.GAFFileVersion_2:
                    report = accGstService.generateTXTGAFV2(paramsJObj);
                    break;
            }
            baos.write(report.toString().getBytes());
            response.setHeader("Content-Disposition", "attachment; filename=\"" + Constants.GAFFileName + gafVersion + ".txt\"");
            response.setContentType("application/octet-stream");
            response.setContentLength(baos.size());
            response.getOutputStream().write(baos.toByteArray());
            response.getOutputStream().flush();
            response.getOutputStream().close();
        } catch (Exception ex) {
            Logger.getLogger(AccGstController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new ModelAndView(Constants.jsonView_ex, Constants.model, jobj.toString());
    }
    
    public ModelAndView exportXMLGAFFile(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        try {
            JSONObject paramsJObj = StringUtil.convertRequestToJsonObject(request);
            String companyid = paramsJObj.optString(Constants.companyKey);
            KwlReturnObject result = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), companyid);
            CompanyAccountPreferences companyAccountPreferences = (CompanyAccountPreferences) result.getEntityList().get(0);
            String gafVersion = companyAccountPreferences.getIafVersion();

            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            Document doc = documentBuilder.newDocument();
            switch (gafVersion) {
                case Constants.GAFFileVersion_1:
                    doc = accGstService.generateXMLGAFV1(paramsJObj);
                    break;
                case Constants.GAFFileVersion_2:
                    doc = accGstService.generateXMLGAFV2(paramsJObj);
                    break;
            }
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            DOMSource dOMSource = new DOMSource(doc);
            StreamResult file = new StreamResult(new File(storageHandlerImpl.GetDocStorePath() + "maleshian.xml"));
            transformer.transform(dOMSource, file);
            response.setHeader("Content-Disposition", "attachment; filename=\"" + Constants.GAFFileName + gafVersion + ".xml\"");
            response.setContentType("application/octet-stream");
            File outFile = new File(storageHandlerImpl.GetDocStorePath() + "maleshian.xml");
            FileInputStream fin = new FileInputStream(outFile);
            byte fileContent[] = new byte[(int) outFile.length()];
            fin.read(fileContent);
            response.getOutputStream().write(fileContent);
            response.getOutputStream().flush();
            response.getOutputStream().close();
        } catch (Exception ex) {
            Logger.getLogger(AccGstController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new ModelAndView(Constants.jsonView_ex, Constants.model, jobj.toString());
    }
   
    /**
     * Function to delete GST Form 03 Generation History.
     * @param request
     * @param response
     * @return
     * @throws AccountingException
     * @throws JSONException
     * @throws Exception 
     */
    public ModelAndView deleteGSTFileGenerationHistory(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jObj = new JSONObject();
        String msg = "";
        Boolean isSuccess = false;
        try {
            JSONObject paramsJObj = StringUtil.convertRequestToJsonObject(request);
            paramsJObj.put("locale", RequestContextUtils.getLocale(request));
            accGstService.deleteGSTFileGenerationHistory(paramsJObj);
            isSuccess = true;
            msg = messageSource.getMessage("acc.gst.deleteSuccessGSTHistory", null, RequestContextUtils.getLocale(request));
        } catch (AccountingException ex) {
            msg = ex.getMessage();
            Logger.getLogger(AccGstController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            msg = ex.getMessage();
            Logger.getLogger(AccGstController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jObj.put("success", isSuccess);
                jObj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(AccGstController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jObj.toString());
    }
    
    /**
     * Function to get validate date period for download(Export) TAP Return file
     * @param request
     * @param response
     * @return
     * @throws JSONException 
     */
      public ModelAndView getValidDateRangeForTAPFileGeneration(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean isSuccess = false;
        try {
            
            Map<String,Object> requestParams = new HashMap<>();
            requestParams.put(Constants.companyid, sessionHandlerImpl.getCompanyid(request));
            requestParams.put(Constants.df, authHandler.getDateOnlyFormat());
            requestParams.put(Constants.language, RequestContextUtils.getLocale(request).getLanguage());
            if (!StringUtil.isNullOrEmpty(request.getParameter("startdate"))) {
                requestParams.put("startdate", request.getParameter("startdate"));
            }
            if (!StringUtil.isNullOrEmpty(request.getParameter("enddate"))) {
                requestParams.put("enddate", request.getParameter("enddate"));
            }

            if (!StringUtil.isNullOrEmpty(request.getParameter(Constants.multiEntityId))) {
                requestParams.put(Constants.multiEntityId, request.getParameter(Constants.multiEntityId));
            }

            if (!StringUtil.isNullOrEmpty(request.getParameter(Constants.multiEntityValue))) {
                requestParams.put(Constants.multiEntityValue, request.getParameter(Constants.multiEntityValue));
            }

            jobj = accGstService.getValidDateRangeForTAPFileGeneration(requestParams);
            isSuccess = jobj.optBoolean(Constants.RES_success, false);
        }catch (ServiceException ex) {
            Logger.getLogger(AccGstController.class.getName()).log(Level.SEVERE, null, ex);
            
        } catch (Exception ex) {
            Logger.getLogger(AccGstController.class.getName()).log(Level.SEVERE, null, ex);
            
        } finally {
            try {
                jobj.put("success", isSuccess);
            } catch (JSONException ex) {
                Logger.getLogger(AccGstController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
}
