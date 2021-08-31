/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.spring.mrp.contractmanagement;

import com.krawler.common.admin.Company;
import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.common.util.Constants;
import com.krawler.common.util.StringUtil;
import com.krawler.esp.handlers.StorageHandler;
import com.krawler.hql.accounting.AccountingException;
import com.krawler.hql.accounting.InvoiceDocuments;
import com.krawler.spring.accounting.handler.AccountingHandlerDAO;
import com.krawler.spring.accounting.invoice.accInvoiceDAO;
import com.krawler.spring.accounting.product.accProductDAO;
import com.krawler.spring.authHandler.authHandler;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.spring.common.fieldDataManager;
import com.krawler.spring.sessionHandler.sessionHandlerImpl;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.io.File;
import java.text.DateFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.fileupload.DiskFileUpload;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.hibernate.TransactionException;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceAware;
import org.springframework.orm.hibernate3.HibernateTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.multiaction.MultiActionController;
import org.springframework.web.servlet.support.RequestContextUtils;

/**
 *
 * @author krawler
 */
public class AccContractManagementControllerCMN extends MultiActionController implements MessageSourceAware {

    private MessageSource messageSource;
    private HibernateTransactionManager txnManager;
    private String successView;
    private AccContractManagementServiceDAOCMN accContractManagementServiceDAOCMNObj;
//    private AccContractManagementDAO accContractManagementDAOObj;
    private AccountingHandlerDAO accountingHandlerDAOobj;
    private fieldDataManager fieldDataManagercntrl;
    private accInvoiceDAO accInvoiceDAOObj;
    private accProductDAO accProductObj;

    public MessageSource getMessageSource() {
        return messageSource;
    }

    public void setMessageSource(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    public HibernateTransactionManager getTxnManager() {
        return txnManager;
    }

    public void setTxnManager(HibernateTransactionManager txnManager) {
        this.txnManager = txnManager;
    }

    public AccContractManagementServiceDAOCMN getAccContractManagementServiceDAOCMNObj() {
        return accContractManagementServiceDAOCMNObj;
    }

    public void setAccContractManagementServiceDAOCMNObj(AccContractManagementServiceDAOCMN accContractManagementServiceDAOCMNObj) {
        this.accContractManagementServiceDAOCMNObj = accContractManagementServiceDAOCMNObj;
    }

//    public AccContractManagementDAO getAccContractManagementDAOObj() {
//        return accContractManagementDAOObj;
//    }
//
//    public void setAccContractManagementDAOObj(AccContractManagementDAO accContractManagementDAOObj) {
//        this.accContractManagementDAOObj = accContractManagementDAOObj;
//    }
    public void setFieldDataManager(fieldDataManager fieldDataManagercntrl) {
        this.fieldDataManagercntrl = fieldDataManagercntrl;
    }
    
    public void setAccInvoiceDAOObj(accInvoiceDAO accInvoiceDAOObj) {
        this.accInvoiceDAOObj = accInvoiceDAOObj;
    }

    public void setAccProductObj(accProductDAO accProductObj) {
        this.accProductObj = accProductObj;
    }

    public void setaccountingHandlerDAO(AccountingHandlerDAO accountingHandlerDAOobj) {
        this.accountingHandlerDAOobj = accountingHandlerDAOobj;
    }

    public String getSuccessView() {
        return successView;
    }

    public void setSuccessView(String successView) {
        this.successView = successView;
    }

    public ModelAndView getMasterContractRows(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        try {

            HashMap<String, Object> requestParams = new HashMap<>();
            requestParams.put("companyid", sessionHandlerImpl.getCompanyid(request));
            requestParams.put("gcurrencyid", sessionHandlerImpl.getCurrencyID(request));
            String closeflag = request.getParameter("closeflag");
            requestParams.put("closeflag", closeflag);
            boolean isForLinking = Boolean.FALSE.parseBoolean(request.getParameter("isForLinking"));
            requestParams.put("isForLinking", isForLinking);
            String soflag = "";
            if (!StringUtil.isNullOrEmpty(request.getParameter("sopolinkflag")) && request.getParameter("sopolinkflag") != null) {
                requestParams.put("sopolinkflag", request.getParameter("sopolinkflag"));
            } else {
                requestParams.put("sopolinkflag", "false");
            }
            requestParams.put("bills", request.getParameter("bills"));
            requestParams.put("dtype", request.getParameter("dtype"));
            boolean isOrder = false;
            String isorder = request.getParameter("isOrder");
            if (!StringUtil.isNullOrEmpty(isorder) && StringUtil.equal(isorder, "true")) {
                isOrder = true;
            }
            requestParams.put("isOrder", isOrder);
            requestParams.put("copyInvoice", request.getParameter("copyInvoice"));
            requestParams.put("dataFormatValue", authHandler.getDateOnlyFormat(request));
            requestParams.put("isLeaseFixedAsset", Boolean.FALSE.parseBoolean(request.getParameter("isLeaseFixedAsset")));
            jobj = accContractManagementServiceDAOCMNObj.getMasterContractRows(requestParams);
            issuccess = true;
        } catch (SessionExpiredException ex) {
            msg = ex.getMessage();
            Logger.getLogger(AccContractManagementControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ServiceException ex) {
            msg = "AccContractManagementControllerCMN.getMasterContractRows:" + ex.getMessage();
            Logger.getLogger(AccContractManagementControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            msg = "AccContractManagementControllerCMN.getMasterContractRows:" + ex.getMessage();
            Logger.getLogger(AccContractManagementControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(AccContractManagementControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    
    public ModelAndView saveMasterContract(HttpServletRequest request, HttpServletResponse response) throws  SessionExpiredException {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("MasterContract_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = null;
        try {
            status = txnManager.getTransaction(def);
            
            String companyid = sessionHandlerImpl.getCompanyid(request);
            DateFormat df = authHandler.getDateOnlyFormat(request);
            Locale locale = RequestContextUtils.getLocale(request);
//            boolean isEdit = false;
//            if(!StringUtil.isNullOrEmpty(request.getParameter("isEdit"))){
//                isEdit = Boolean.parseBoolean(request.getParameter("isEdit"));
//            }
            String mastercontractid = "";
            if(!StringUtil.isNullOrEmpty(request.getParameter("mastercontractid"))){
                mastercontractid = request.getParameter("mastercontractid");//mastercontractid - will be non empty in edit case only
            }
            
            String jsonDataStr = "{}";
            if (!StringUtil.isNullOrEmpty(request.getParameter("contractdetailsdata"))) {
                /*
                data of global fields in cotract details tab
                */
                jsonDataStr = request.getParameter("contractdetailsdata");
            }
            String paymenttermsdata = "{}";
            if (!StringUtil.isNullOrEmpty(request.getParameter("paymenttermsdata"))) {
                /*
                    Payment terms tab data
                */
                paymenttermsdata = request.getParameter("paymenttermsdata");
            }
            String billingcontractdata = "{}";
            if (!StringUtil.isNullOrEmpty(request.getParameter("billingcontractdata"))) {
                /*
                Billing tab data
                */
                billingcontractdata = request.getParameter("billingcontractdata");
            }
            String documentrequireddata = "{}";
            if (!StringUtil.isNullOrEmpty(request.getParameter("documentrequireddata"))) {
                /*
                Document required tab
                */
                documentrequireddata = request.getParameter("documentrequireddata");
            }
            String details = "{}";
            if (!StringUtil.isNullOrEmpty(request.getParameter("detailsdata"))) {
                /*
                Data of product grid in contract details and shipment contract tab
                */
                details = request.getParameter("detailsdata");
            }
            String customfield = "[]";
            if (request.getParameter("customfield") != null) {
                customfield = request.getParameter("customfield");
            }
            
            Map<String, Object> requestParams = new HashMap<>();
            requestParams.put("contractdetailsdata", jsonDataStr);
            requestParams.put("billingcontractdata", billingcontractdata);
            requestParams.put("paymenttermsdata", paymenttermsdata);
            requestParams.put("documentrequireddata", documentrequireddata);
            requestParams.put("details", details);
            requestParams.put(Constants.df, df);
            requestParams.put("companyid", companyid);
            requestParams.put("locale", locale);
//            requestParams.put("isEdit", isEdit);
            requestParams.put("mastercontractid", mastercontractid);
            requestParams.put("customfield", customfield);
            jobj = accContractManagementServiceDAOCMNObj.saveMasterContract(requestParams);
            
            txnManager.commit(status);
            issuccess= true;
        } catch (ServiceException | TransactionException ex) {
            if (status != null) {
                txnManager.rollback(status);
            }

            Logger.getLogger(AccContractManagementController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } finally {
            try {
                jobj.put("success", issuccess);
            } catch (JSONException ex) {
                Logger.getLogger(AccContractManagementController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        return new ModelAndView("jsonView", "model", jobj.toString());

    }
    
    public ModelAndView attachDocuments(HttpServletRequest request, HttpServletResponse response) {
        String msg = "";
        Boolean success = false;
        JSONObject jobj = new JSONObject();
        JSONObject finalObject = new JSONObject();
        String savedFilesMappingId = "";
        String savedFilesId = "";
        String[] returnData = new String[2];
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("CF_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);

        TransactionStatus status = txnManager.getTransaction(def);
        try {
            returnData = uploadDoc(request);
            savedFilesMappingId = returnData[0];
            savedFilesId = returnData[1];
            success = true;
            msg = messageSource.getMessage("acc.mrp.field.fileUploadedSuccess", null, RequestContextUtils.getLocale(request));
            txnManager.commit(status);
        } catch (ServiceException ex) {
            success = false;
            msg = ex.getMessage();
            txnManager.rollback(status);
            Logger.getLogger(AccContractManagementController.class.getName()).log(Level.SEVERE, "saveUsers", ex);
        } catch (Exception ex) {
            msg = "" + ex.getMessage();
            success = false;
            txnManager.rollback(status);
            Logger.getLogger(AccContractManagementController.class.getName()).log(Level.SEVERE, "saveUsers", ex);
        } finally {
            try {
                jobj.put("success", success);
                jobj.put("msg", msg);
                jobj.put("savedFilesMappingId", savedFilesMappingId);
                jobj.put("file", savedFilesId);
                finalObject.put("data", jobj);
            } catch (com.krawler.utils.json.base.JSONException ex) {
                Logger.getLogger(AccContractManagementController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView_ex", "model", finalObject.toString());
    }

    public String[] uploadDoc(HttpServletRequest request) throws ServiceException, AccountingException {
        String[] returnData = new String[2];
        String savedFilesMappingId = "";
        String savedFilesId = "";
        try {
            String result = "";
            Boolean fileflag = false;
            String fileName = "";
            boolean isUploaded;
            String Ext;
            final String sep = StorageHandler.GetFileSeparator();
            DiskFileUpload fu = new DiskFileUpload();
            java.util.List fileItems = null;
            FileItem fi = null;
            String companyid = sessionHandlerImpl.getCompanyid(request);
            savedFilesMappingId = request.getParameter("savedFilesMappingId");
            try {
                fileItems = fu.parseRequest(request);
            } catch (FileUploadException e) {
                throw ServiceException.FAILURE("ProfileHandler.updateProfile", e);
            }
            java.util.HashMap arrParam = new java.util.HashMap();
            for (java.util.Iterator k = fileItems.iterator(); k.hasNext();) {
                fi = (FileItem) k.next();
                arrParam.put(fi.getFieldName(), fi.getString());
                if (!fi.isFormField()) {
                    if (fi.getSize() != 0) {
                        fileflag = true;
                        fileName = new String(fi.getName().getBytes());
                    } else {
                        throw new AccountingException("File not uploaded! File should not be empty.");    //When file is empty
                    }
                }
            }

            if (fileflag) {
                try {
                    String storePath = StorageHandler.GetDocStorePath();
                    File destDir = new File(storePath);
                    if (!destDir.exists()) {
                        destDir.mkdirs();
                    }
                    int doccount = 0;
                    fu = new DiskFileUpload();
                    fu.setSizeMax(-1);
                    fu.setSizeThreshold(4096);
                    fu.setRepositoryPath(storePath);
                    for (Iterator i = fileItems.iterator(); i.hasNext();) {
                        fi = (FileItem) i.next();
                        if (!fi.isFormField() && fi.getSize() != 0 && doccount < 3) {
                            Ext = "";
                            doccount++;//ie 8 fourth file gets attached				
                            String filename = UUID.randomUUID().toString();
                            try {
                                fileName = new String(fi.getName().getBytes(), "UTF8");
                                if (fileName.contains(".")) {
                                    Ext = fileName.substring(fileName.lastIndexOf("."));
                                }
                                if (fi.getSize() != 0) {
                                    isUploaded = true;
                                    File uploadFile = new File(storePath + sep
                                            + filename + Ext);
                                    fi.write(uploadFile);

                                    InvoiceDocuments document = new InvoiceDocuments();
                                    document.setDocID(filename);
                                    document.setDocName(fileName);
                                    document.setDocType("");
                                    KwlReturnObject cmp = accountingHandlerDAOobj.getObject(Company.class.getName(), companyid);
                                    Company company = (Company) cmp.getEntityList().get(0);
                                    Map<String, Object> fileMap = new HashMap<>();
                                    if (StringUtil.isNullOrEmpty(savedFilesMappingId)) {
                                        savedFilesMappingId = UUID.randomUUID().toString();
                                    } else {
                                        KwlReturnObject savedFilesIdResult = accInvoiceDAOObj.getDocumentIdFromMappingId(savedFilesMappingId, companyid);
                                        List savedFilesList = savedFilesIdResult.getEntityList();
                                        Iterator itr = savedFilesList.iterator();
                                        while (itr.hasNext()) {
                                            savedFilesId += itr.next().toString() + ",";
                                        }
                                    }
                                    fileMap.put("id", savedFilesMappingId);
                                    fileMap.put("companyid", companyid);
                                    if (arrParam.containsKey("contractid") && !StringUtil.isNullOrEmpty(arrParam.get("contractid").toString())) {
                                        fileMap.put("contractid", arrParam.get("contractid").toString());
                                    } else {
                                        fileMap.put("contractid", "");
                                    }

                                    HashMap<String, Object> hashMap = new HashMap<String, Object>();
                                    hashMap.put("InvoceDocument",document);
                                    accInvoiceDAOObj.saveinvoiceDocuments(hashMap);
                                    fileMap.put("documentid", document.getID());
                                    accContractManagementServiceDAOCMNObj.saveFileMapping(fileMap);
                                    savedFilesId += document.getID() + ",";
                                } else {
                                    isUploaded = false;
                                }
                            } catch (Exception e) {
                                Logger.getLogger(AccContractManagementServiceImpl.class.getName()).log(Level.SEVERE, null, e);
                                throw ServiceException.FAILURE("AccContractManagementServiceImpl.uploadDoc", e);
                            }
                        }
                    }
                } catch (Exception ex) {
                    Logger.getLogger(AccContractManagementServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
                    throw ServiceException.FAILURE("AccContractManagementServiceImpl.uploadDoc", ex);
                }
            }
        } catch (AccountingException ae) {
            throw new AccountingException("File not uploaded! File should not be empty.");
        } catch (Exception ex) {
            Logger.getLogger(AccContractManagementServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE("AccContractManagementServiceImpl.uploadDoc", ex);
        }
        returnData[0] = savedFilesMappingId;
        if (!StringUtil.isNullOrEmpty(savedFilesId)) {
            savedFilesId = savedFilesId.substring(0, savedFilesId.length() - 1);
        }
        returnData[1] = savedFilesId;
        return returnData;
    }
}
