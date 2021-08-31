/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.inventory.view;

import com.krawler.common.admin.Company;
import com.krawler.common.service.ServiceException;
import com.krawler.common.util.InventoryModules;
import com.krawler.common.util.StringUtil;
import com.krawler.esp.handlers.StorageHandler;
import com.krawler.hql.accounting.AccountingException;
import com.krawler.inventory.model.fileuploaddownlaod.InvDocumentService;
import com.krawler.inventory.model.fileuploaddownlaod.InventoryDocumentCompMap;
import com.krawler.inventory.model.fileuploaddownlaod.InventoryDocuments;
import com.krawler.inventory.model.location.LocationService;
import com.krawler.inventory.model.sequence.SeqService;
import com.krawler.inventory.model.stockout.StockAdjustmentService;
import com.krawler.inventory.model.store.StoreService;
import com.krawler.spring.accounting.handler.AccountingHandlerDAO;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.spring.sessionHandler.sessionHandlerImpl;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONObject;
import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.fileupload.DiskFileUpload;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
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



public class InvCommonController extends MultiActionController implements MessageSourceAware{

    private static final DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
    private static final Logger lgr = Logger.getLogger(StockAdjustmentController.class.getName());
    private HibernateTransactionManager txnManager;
    private String successView;
    private AccountingHandlerDAO accountingHandlerDAO;
    private StoreService storeService;
    private LocationService locationService;
    private MessageSource messageSource;
    private InvDocumentService documentService;

    public void setTxnManager(HibernateTransactionManager txnManager) {
        this.txnManager = txnManager;
    }

    public void setSuccessView(String successView) {
        this.successView = successView;
    }

    public void setStoreService(StoreService storeService) {
        this.storeService = storeService;
    }

    public void setAccountingHandlerDAO(AccountingHandlerDAO accountingHandlerDAO) {
        this.accountingHandlerDAO = accountingHandlerDAO;
    }

    public void setLocationService(LocationService locationService) {
        this.locationService = locationService;
    }

    public void setDocumentService(InvDocumentService documentService) {
        this.documentService = documentService;
    }
 
    
    
    @Override
    public void setMessageSource(MessageSource ms) {
            this.messageSource=ms;
    }

    public ModelAndView attachDocuments(HttpServletRequest request, HttpServletResponse response) {
        String msg = "";
        Boolean success = false;
        JSONObject jobj = new JSONObject();
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("CF_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);

        TransactionStatus status = txnManager.getTransaction(def);
        try {
            uploadDoc(request);
            success = true;
            msg = messageSource.getMessage("acc.invoiceList.bt.fileUploadedSuccess", null, RequestContextUtils.getLocale(request));
            txnManager.commit(status);
        } catch (ServiceException ex) {
            success = false;
            msg = ex.getMessage();
            txnManager.rollback(status);
            Logger.getLogger(InvCommonController.class.getName()).log(Level.SEVERE, "saveUsers", ex);
        } catch (Exception ex) {
            msg = "" + ex.getMessage();
            success = false;
            txnManager.rollback(status);
            Logger.getLogger(InvCommonController.class.getName()).log(Level.SEVERE, "saveUsers", ex);
        } finally {
            try {
                jobj.put("success", success);
                jobj.put("msg", msg);
            } catch (com.krawler.utils.json.base.JSONException ex) {
                Logger.getLogger(InvCommonController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView_ex", "model", jobj.toString());
    }

    public void uploadDoc(HttpServletRequest request) throws ServiceException, AccountingException {
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

                                    InventoryDocuments document = new InventoryDocuments();
                                    document.setDocID(filename);
                                    document.setDocName(fileName);
                                    document.setDocType("");
                                    
                                    KwlReturnObject cmp = accountingHandlerDAO.getObject(Company.class.getName(), companyid);
                                    Company company = (Company) cmp.getEntityList().get(0);
                                    InventoryModules im = null;
                                    String moduleWiseMainIdList = null;
                                    String[] moduleWiseMainId = {};
                                    
                                    if (!StringUtil.isNullOrEmpty(arrParam.get("modulewisemainid").toString())) {
                                        moduleWiseMainIdList = arrParam.get("modulewisemainid").toString();
                                        if (!StringUtil.isNullOrEmpty(moduleWiseMainIdList)) {
                                            moduleWiseMainId = moduleWiseMainIdList.split(",");
                                        }
                                    }
                                    if (!StringUtil.isNullOrEmpty(arrParam.get("modulename").toString())) {
                                        im = InventoryModules.valueOf(arrParam.get("modulename").toString());
                                    }
                                    
                                    for (String mainId : moduleWiseMainId) {

                                        InventoryDocumentCompMap inventoryDocumentMap = new InventoryDocumentCompMap();
                                        inventoryDocumentMap.setDocument(document);
                                        inventoryDocumentMap.setCompany(company);
                                        inventoryDocumentMap.setModuleWiseId(mainId);
                                        if (im != null) {
                                            inventoryDocumentMap.setModule(im);
                                        }

                                        HashMap<String, Object> hashMap = new HashMap<String, Object>();
                                        hashMap.put("InventoryDocument", document);
                                        hashMap.put("InventoryDocumentMapping", inventoryDocumentMap);
                                        documentService.saveInventoryDocuments(hashMap);
                                    }

                                } else {
                                    isUploaded = false;
                                }
                            } catch (Exception e) {
                                Logger.getLogger(InvCommonController.class.getName()).log(Level.SEVERE, null, e);
                                throw ServiceException.FAILURE("InvCommonController.uploadDoc", e);
                            }
                        }
                    }
                } catch (Exception ex) {
                    Logger.getLogger(InvCommonController.class.getName()).log(Level.SEVERE, null, ex);
                    throw ServiceException.FAILURE("InvCommonController.uploadDoc", ex);
                }
            }
        } catch (AccountingException ae) {
            throw new AccountingException("File not uploaded! File should not be empty.");
        } catch (Exception ex) {
            Logger.getLogger(InvCommonController.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE("InvCommonController.uploadDoc", ex);
        }
    }

    public ModelAndView getAttachedDocuments(HttpServletRequest request, HttpServletResponse response) {
        String msg = "";
        Boolean success = false;
        JSONObject jobj = new JSONObject();
        JSONArray jSONArray = new JSONArray();
        JSONObject finalJSONObject = new JSONObject();
        int count = 0;
        try {
            String companyid = sessionHandlerImpl.getCompanyid(request);
            String moduleWiseMainID = request.getParameter("id");
            String moduleName = request.getParameter("modulename");
            String start = request.getParameter("start");
            String limit = request.getParameter("limit");
            HashMap<String, Object> hashMap = new HashMap<String, Object>();
            hashMap.put("modulewiseid", moduleWiseMainID);
            hashMap.put("modulename", moduleName);
            hashMap.put("companyid", companyid);
            hashMap.put("start", start);
            hashMap.put("limit", limit);
            KwlReturnObject object = documentService.getInventoryDocuments(hashMap);

            Iterator iterator = object.getEntityList().iterator();
            while (iterator.hasNext()) {
                Object[] obj = (Object[]) iterator.next();
                JSONObject jSONObject = new JSONObject();
                jSONObject.put("docname", obj[0]);
                jSONObject.put("docid", obj[2]);
                jSONObject.put("doctypeid", obj[1]);
                jSONArray.put(jSONObject);
                count++;
            }

            finalJSONObject.put("count", count);
            finalJSONObject.put("data", jSONArray);
            success = true;
        } catch (ServiceException ex) {
            success = false;
            msg = ex.getMessage();
            Logger.getLogger(InvCommonController.class.getName()).log(Level.SEVERE, "saveUsers", ex);
        } catch (Exception ex) {
            msg = "" + ex.getMessage();
            success = false;
            Logger.getLogger(InvCommonController.class.getName()).log(Level.SEVERE, "saveUsers", ex);
        } finally {
            try {
                jobj.put("data", finalJSONObject);
                jobj.put("valid", success);
            } catch (com.krawler.utils.json.base.JSONException ex) {
                Logger.getLogger(InvCommonController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView_ex", "model", jobj.toString());
    }

    public ModelAndView deleteDocument(HttpServletRequest request, HttpServletResponse response) {
        String msg = "";
        Boolean success = false;
        JSONObject jobj = new JSONObject();
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("CF_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);

        TransactionStatus status = txnManager.getTransaction(def);
        try {
            String docID = request.getParameter("docid");
            if (!StringUtil.isNullOrEmpty(docID)) {
                KwlReturnObject object = documentService.deleteInventoryDocument(docID);
                success = true;
                msg = object.getMsg();
                
            }
          txnManager.commit(status);
        } catch (ServiceException ex) {
            success = false;
            msg = ex.getMessage();
            txnManager.rollback(status);
            Logger.getLogger(InvCommonController.class.getName()).log(Level.SEVERE, "saveUsers", ex);
        } catch (Exception ex) {
            msg = "" + ex.getMessage();
            success = false;
            txnManager.rollback(status);
            Logger.getLogger(InvCommonController.class.getName()).log(Level.SEVERE, "saveUsers", ex);
        } finally {
            try {

                JSONObject jobj1 = new JSONObject();
                jobj1.put("msg", msg);
                jobj1.put("success", success);
                jobj.append("data", jobj1);
                jobj.put("valid", true);
            } catch (com.krawler.utils.json.base.JSONException ex) {
                Logger.getLogger(InvCommonController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView_ex", "model", jobj.toString());
    }
}
