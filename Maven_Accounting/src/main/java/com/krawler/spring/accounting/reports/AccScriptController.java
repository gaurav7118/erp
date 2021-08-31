/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.spring.accounting.reports;

import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.common.util.Constants;
import com.krawler.common.util.StringUtil;
import com.krawler.spring.accounting.purchaseorder.accPurchaseOrderDAO;
import com.krawler.spring.authHandler.authHandler;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.spring.companyDetails.companyDetailsDAO;
import com.krawler.spring.exportFunctionality.exportMPXDAOImpl;
import com.krawler.spring.storageHandler.storageHandlerImpl;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.io.FileUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
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
public class AccScriptController extends MultiActionController implements MessageSourceAware {

    private MessageSource messageSource;
    private HibernateTransactionManager txnManager;
    private accPurchaseOrderDAO accPurchaseOrderobj;
    private AccScriptDao accScriptDao;
    private AccScriptService accScriptService;
    private companyDetailsDAO companyDetailsDAOObj;
    private exportMPXDAOImpl exportDaoObj;

     public void setcompanyDetailsDAO(companyDetailsDAO companyDetailsDAOObj) {
        this.companyDetailsDAOObj = companyDetailsDAOObj;
    }
    public void setAccScriptService(AccScriptService accScriptService) {
        this.accScriptService = accScriptService;
    }

    public void setAccScriptDao(AccScriptDao accScriptDao) {
        this.accScriptDao = accScriptDao;
    }

    @Override
    public void setMessageSource(MessageSource ms) {
        this.messageSource = ms;

    }

    public void setTxnManager(HibernateTransactionManager txnManager) {
        this.txnManager = txnManager;
    }

    public void setaccPurchaseOrderDAO(accPurchaseOrderDAO accPurchaseOrderobj) {
        this.accPurchaseOrderobj = accPurchaseOrderobj;
    }
    public void setexportMPXDAOImpl(exportMPXDAOImpl exportDaoObj) {
        this.exportDaoObj = exportDaoObj;
    }
    public ModelAndView FindCorruptDebitCreditNote(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("PO_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        try {
            String subdomain = "";
            String[] subdomainArray = null;
            String filename = "";
            if (!StringUtil.isNullOrEmpty(request.getParameter("filename"))) {
                filename = request.getParameter("filename");
            }
            if (!StringUtil.isNullOrEmpty(request.getParameter("subdomain"))) {
                subdomain = request.getParameter("subdomain").toString().trim();
                subdomainArray = subdomain.split(",");
            }
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            KwlReturnObject rCompanyId = null;
            rCompanyId = accPurchaseOrderobj.getAllCompanyFromDb(subdomainArray);
            Iterator itrCompanyId = rCompanyId.getEntityList().iterator();

            while (itrCompanyId.hasNext()) {
                String companyid = (String) itrCompanyId.next();
                requestParams.put("companyid", companyid);
                if (!StringUtil.isNullOrEmpty(filename)) {
                    requestParams.put("filename", filename);
                }
                Map map = accScriptDao.getDebitNoteAndDntaxEntry(requestParams);
//                List l = (List) map.get("dntaxentrydata");
//                List l2 = (List) map.get("corruptdn");
//                jobj.put("\n List of Corrupt DN", l2);
//                map = accScriptDao.getCreditNoteAndCNtaxEntry(requestParams);
//                l = (List) map.get("cntaxentrydata");
//                l2 = (List) map.get("corruptcn");
//                jobj.put("\n List of Corrupt CN", l2);
            }

            issuccess = true;
            txnManager.commit(status);
        } catch (Exception ex) {
            if (status != null) {
                txnManager.rollback(status);
            }
            ex.printStackTrace();
            msg = "" + ex.getMessage();
            if (ex.getMessage() == null) {
                msg = ex.getCause().getMessage();
            }
            Logger.getLogger(AccScriptController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", "Script completed for finding corrupt data");

            } catch (JSONException ex) {
                Logger.getLogger(AccScriptController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public ModelAndView   cleanUpCompanyData(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("PO_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        try {
            String subdomain = "";
           String dbname="";
            String companyid = "";
            String[] subdomainArray = null;
            String filename = "";
          
            if (!StringUtil.isNullOrEmpty(request.getParameter("subdomain"))) {
                subdomain = request.getParameter("subdomain").toString().trim();
                subdomainArray = subdomain.split(",");
            }
            if (!StringUtil.isNullOrEmpty(request.getParameter("dbname"))) {
                dbname = request.getParameter("dbname").toString();
                
            }
          for(int i=0;i<subdomainArray.length;i++)
          {
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
                
           requestParams.put("dbname", dbname);
           requestParams.put("subdomain", subdomainArray[i]);
          
            accScriptDao.cleanUpCompanyData(requestParams);
            
          }
           
            issuccess = true;
            txnManager.commit(status);
        } catch (Exception ex) {
            if (status != null) {
                txnManager.rollback(status);
            }
            ex.printStackTrace();
            msg = "" + ex.getMessage();
            if (ex.getMessage() == null) {
                msg = ex.getCause().getMessage();
            }
            Logger.getLogger(AccScriptController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", "Script completed for deleting company data");

            } catch (JSONException ex) {
                Logger.getLogger(AccScriptController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public ModelAndView getTrnsactionsOtherThanControlAccountForVendor(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("PO_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        try {
            String subdomain = "";
            String[] subdomainArray = null;
            if (!StringUtil.isNullOrEmpty(request.getParameter("subdomain"))) {
                subdomain = request.getParameter("subdomain").toString().trim();
                subdomainArray = subdomain.split(",");
            }
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            KwlReturnObject rCompanyId = null;
            rCompanyId = accPurchaseOrderobj.getAllCompanyFromDb(subdomainArray);
            Iterator itrCompanyId = rCompanyId.getEntityList().iterator();
            while (itrCompanyId.hasNext()) {
                String companyid = (String) itrCompanyId.next();
                requestParams.put("companyid", companyid);
                accScriptService.getTrnsactionsOtherThanControlAccountForVendor(requestParams);
            }
            issuccess = true;
            txnManager.commit(status);
        } catch (Exception ex) {
            if (status != null) {
                txnManager.rollback(status);
            }
            ex.printStackTrace();
            msg = "" + ex.getMessage();
            if (ex.getMessage() == null) {
                msg = ex.getCause().getMessage();
            }
            Logger.getLogger(AccScriptController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", "Script completed for find Trnsactions");

            } catch (JSONException ex) {
                Logger.getLogger(AccScriptController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public ModelAndView getTrnsactionsOtherThanControlAccountForCustomer(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("PO_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        try {
            String subdomain = "";
            String[] subdomainArray = null;
            if (!StringUtil.isNullOrEmpty(request.getParameter("subdomain"))) {
                subdomain = request.getParameter("subdomain").toString().trim();
                subdomainArray = subdomain.split(",");
            }
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            KwlReturnObject rCompanyId = null;
            rCompanyId = accPurchaseOrderobj.getAllCompanyFromDb(subdomainArray);
            Iterator itrCompanyId = rCompanyId.getEntityList().iterator();
            while (itrCompanyId.hasNext()) {
                String companyid = (String) itrCompanyId.next();
                requestParams.put("companyid", companyid);
                accScriptService.getTrnsactionsOtherThanControlAccountForCustomer(requestParams);
            }
            issuccess = true;
            txnManager.commit(status);
        } catch (Exception ex) {
            if (status != null) {
                txnManager.rollback(status);
            }
            ex.printStackTrace();
            msg = "" + ex.getMessage();
            if (ex.getMessage() == null) {
                msg = ex.getCause().getMessage();
            }
            Logger.getLogger(AccScriptController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", "Script completed for find Trnsactions");

            } catch (JSONException ex) {
                Logger.getLogger(AccScriptController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public ModelAndView exportOpeningDocumentListForAccount(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        try {
            String subdomain = "";
            String[] subdomainArray = null;
            if (!StringUtil.isNullOrEmpty(request.getParameter("subdomain"))) {
                subdomain = request.getParameter("subdomain").toString().trim();
                subdomainArray = subdomain.split(",");
            }
            String accname = "";
            if (!StringUtil.isNullOrEmpty(request.getParameter("accname"))) {
                accname = request.getParameter("accname");
            }
            String filename = "";
            if (!StringUtil.isNullOrEmpty(request.getParameter("filename"))) {
                filename = request.getParameter("filename");
            }
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            KwlReturnObject rCompanyId = null;
            rCompanyId = accPurchaseOrderobj.getAllCompanyFromDb(subdomainArray);
            Iterator itrCompanyId = rCompanyId.getEntityList().iterator();
            while (itrCompanyId.hasNext()) {
                String companyid = (String) itrCompanyId.next();
                requestParams.put("companyid", companyid);
                if (!StringUtil.isNullOrEmpty(filename)) {
                    requestParams.put("filename", filename);
                }
                if (!StringUtil.isNullOrEmpty(accname)) {
                    requestParams.put("accountname", accname);
                }
                Map map = new HashMap();
                map = accScriptDao.getOpeningDocumentListForVendor(requestParams);
                map = accScriptDao.getOpeningDocumentListForCustomer(requestParams);
            }
            issuccess = true;
        } catch (Exception ex) {
            ex.printStackTrace();
            msg = "" + ex.getMessage();
            if (ex.getMessage() == null) {
                msg = ex.getCause().getMessage();
            }
            Logger.getLogger(AccScriptController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", "Script completed for find Trnsactions and file has been saved in specified Location");

            } catch (JSONException ex) {
                Logger.getLogger(AccScriptController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public ModelAndView getCNDNForGainLossNotPosted(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        try {
            String subdomain = "";
            String[] subdomainArray = null;
            if (!StringUtil.isNullOrEmpty(request.getParameter("subdomain"))) {
                subdomain = request.getParameter("subdomain").toString().trim();
                subdomainArray = subdomain.split(",");
            }
            String filename = "";
            if (!StringUtil.isNullOrEmpty(request.getParameter("filename"))) {
                filename = request.getParameter("filename");
            }
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            KwlReturnObject rCompanyId = null;
            rCompanyId = accPurchaseOrderobj.getAllCompanyFromDb(subdomainArray);
            Iterator itrCompanyId = rCompanyId.getEntityList().iterator();
            while (itrCompanyId.hasNext()) {
                String companyid = (String) itrCompanyId.next();
                requestParams.put("companyid", companyid);
                if (!StringUtil.isNullOrEmpty(filename)) {
                    requestParams.put("filename", filename);
                }
                Map map = new HashMap();
                map = accScriptService.getCNDNForGainLossNotPosted(requestParams);
            }
            issuccess = true;
        } catch (Exception ex) {
            ex.printStackTrace();
            msg = "" + ex.getMessage();
            if (ex.getMessage() == null) {
                msg = ex.getCause().getMessage();
            }
            Logger.getLogger(AccScriptController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", "Script completed for find Trnsactions and file has been saved in specified Location");

            } catch (JSONException ex) {
                Logger.getLogger(AccScriptController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public ModelAndView getPaymentReceiptForGainLossNotPosted(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        try {
            String subdomain = "";
            String[] subdomainArray = null;
            if (!StringUtil.isNullOrEmpty(request.getParameter("subdomain"))) {
                subdomain = request.getParameter("subdomain").toString().trim();
                subdomainArray = subdomain.split(",");
            }
            String filename = "";
            if (!StringUtil.isNullOrEmpty(request.getParameter("filename"))) {
                filename = request.getParameter("filename");
            }
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            KwlReturnObject rCompanyId = null;
            rCompanyId = accPurchaseOrderobj.getAllCompanyFromDb(subdomainArray);
            Iterator itrCompanyId = rCompanyId.getEntityList().iterator();
            while (itrCompanyId.hasNext()) {
                String companyid = (String) itrCompanyId.next();
                requestParams.put("companyid", companyid);
                if (!StringUtil.isNullOrEmpty(filename)) {
                    requestParams.put("filename", filename);
                }
                Map map = new HashMap();
                map = accScriptService.getPaymentReceiptForGainLossNotPosted(requestParams);
            }
            issuccess = true;
        } catch (Exception ex) {
            ex.printStackTrace();
            msg = "" + ex.getMessage();
            if (ex.getMessage() == null) {
                msg = ex.getCause().getMessage();
            }
            Logger.getLogger(AccScriptController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", "Script completed for find Trnsactions and file has been saved in specified Location");

            } catch (JSONException ex) {
                Logger.getLogger(AccScriptController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public ModelAndView getScriptFiles(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        try {
            String subdomain = "";
            String[] subdomainArray = null;
            if (!StringUtil.isNullOrEmpty(request.getParameter("subdomain"))) {
                subdomain = request.getParameter("subdomain").toString().trim();
                subdomainArray = subdomain.split(",");
            }
            File folder = new File("/home/krawler/ScriptData");
            folder.mkdir();
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            KwlReturnObject rCompanyId = null;
            rCompanyId = accPurchaseOrderobj.getAllCompanyFromDb(subdomainArray);
            Iterator itrCompanyId = rCompanyId.getEntityList().iterator();
            while (itrCompanyId.hasNext()) {
                String companyid = (String) itrCompanyId.next();
                requestParams.put("companyid", companyid);
                Map map = new HashMap();
                map = accScriptDao.getDebitNoteAndDntaxEntry(requestParams);
                issuccess = map.containsKey("success") ? Boolean.parseBoolean(map.get("success").toString()) : false;
                if (issuccess) {
                    msg += "Script completed to find Corrupt DN Trnsactions \n";
                }
                accScriptService.getTrnsactionsOtherThanControlAccountForVendor(requestParams);
                issuccess = map.containsKey("success") ? Boolean.parseBoolean(map.get("success").toString()) : false;
                if (issuccess) {
                    msg += "Script completed to find Transactions Other than Control Account For Vendor \n";
                }

                accScriptService.getTrnsactionsOtherThanControlAccountForCustomer(requestParams);
                issuccess = map.containsKey("success") ? Boolean.parseBoolean((String) map.get("success").toString()) : false;
                if (issuccess) {
                    msg += "Script completed to find Transactions Other than Control Account For Customer \n";
                }

                map = accScriptDao.getOpeningDocumentListForVendor(requestParams);
                issuccess = map.containsKey("success") ? Boolean.parseBoolean((String) map.get("success").toString()) : false;
                if (issuccess) {
                    msg += "Script completed to get Opening document for vendor \n";
                }
                map = accScriptDao.getOpeningDocumentListForCustomer(requestParams);
                issuccess = map.containsKey("success") ? Boolean.parseBoolean((String) map.get("success").toString()) : false;
                if (issuccess) {
                    msg += "Script completed to get Opening document for customer \n";
                }

                map = accScriptService.getCNDNForGainLossNotPosted(requestParams);
                issuccess = map.containsKey("success") ? Boolean.parseBoolean((String) map.get("success").toString()) : false;
                if (issuccess) {
                    msg += "Script completed to find CN/DN Transaction for which Gain Loss not posted \n";
                }

                map = accScriptService.getPaymentReceiptForGainLossNotPosted(requestParams);
                issuccess = map.containsKey("success") ? Boolean.parseBoolean((String) map.get("success").toString()) : false;
                if (issuccess) {
                    msg += "Script completed to find MP/RP Transaction for which Gain Loss not posted \n";
                }

                accScriptService.getJournalEntryRecordForControlAccounts(requestParams);
                issuccess = map.containsKey("success") ? Boolean.parseBoolean((String) map.get("success").toString()) : false;
                if (issuccess) {
                    msg += "Script completed to find JE Created using Vendors account \n";
                }

                accScriptService.getInvoicesAmountDiffThanJEAmount(requestParams);
                issuccess = map.containsKey("success") ? Boolean.parseBoolean((String) map.get("success").toString()) : false;
                if (issuccess) {
                    msg += "Script completed to find Invoices Records with Diff JE and Invoice Amount \n";
                }

                accScriptService.getDifferentPaymentReceiptAndGainLossJEAccount(requestParams);
                issuccess = map.containsKey("success") ? Boolean.parseBoolean((String) map.get("success").toString()) : false;
                if (issuccess) {
                    msg += "Script completed to find Payment Records with Gain Loss account different than Vendors Account \n";
                }

            }
            issuccess = true;
        } catch (Exception ex) {
            ex.printStackTrace();
            msg = "" + ex.getMessage();
            if (ex.getMessage() == null) {
                msg = ex.getCause().getMessage();
            }
            Logger.getLogger(AccScriptController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);

            } catch (JSONException ex) {
                Logger.getLogger(AccScriptController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }/**
     * 
     * @param request
     * @param response
     * @return 
     * @Desc :  Script for copy master data to dimension
     */
    public ModelAndView copyDataFromMasterToDimension(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        String responselist = "";
        KwlReturnObject kwlReturnObject = null;
        try {
            JSONObject params = StringUtil.convertRequestToJsonObject(request);
            accScriptService.copyDataFromMasterToDimension(params);
            issuccess = true;
            msg = "Script Executed Successfully  ";
            responselist = params.optString("responselist");
        } catch (Exception ex) {
            ex.printStackTrace();
            msg = "" + ex.getMessage();
            if (ex.getMessage() == null) {
                msg = ex.getCause().getMessage();
            }
            Logger.getLogger(AccScriptController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg + responselist);

            } catch (JSONException ex) {
                Logger.getLogger(AccScriptController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    /**
     * Function to Insert GST Fields History data for India 
     * @param request
     * @param response
     * @return 
     */
    public ModelAndView insertGSTFieldsData(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        String responselist = "";
        KwlReturnObject kwlReturnObject = null;
        try {
            JSONObject params = StringUtil.convertRequestToJsonObject(request);
            accScriptService.insertGSTFieldsData(params);
            issuccess = true;
            msg = "Script Executed Successfully  ";
        } catch (Exception ex) {
            ex.printStackTrace();
            msg = "" + ex.getMessage();
            if (ex.getMessage() == null) {
                msg = ex.getCause().getMessage();
            }
            Logger.getLogger(AccScriptController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg + responselist);

            } catch (JSONException ex) {
                Logger.getLogger(AccScriptController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    /**
     * Function to update transaction created without state i.e State dimension values is empty.
     * @param request
     * @param response
     * @return 
     */
        public ModelAndView updateGSTTransactions(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        String responselist = "";
        try {
            JSONObject params = StringUtil.convertRequestToJsonObject(request);
            jobj = accScriptService.updateGSTTransactions(params);
            issuccess = true;
            msg = "Script Executed Successfully  for "+jobj.optString("msg");
        } catch (Exception ex) {
            ex.printStackTrace();
            msg = "" + ex.getMessage();
            if (ex.getMessage() == null) {
                msg = ex.getCause().getMessage();
            }
            Logger.getLogger(AccScriptController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg + responselist);

            } catch (JSONException ex) {
                Logger.getLogger(AccScriptController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    public ModelAndView updateMailidsLocally(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        int count=0,companyCount = 0;
        String EmailID = "";
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("PO_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        try {
            String subdomain = "";
            String[] subdomainArray = null;
            Map map = new HashMap();
            if (!StringUtil.isNullOrEmpty(request.getParameter("subdomain"))) {
                subdomain = request.getParameter("subdomain").toString().trim();
                subdomainArray = subdomain.split(",");
}
            if (!StringUtil.isNullOrEmpty(request.getParameter("emailid"))) {
                EmailID = request.getParameter("emailid").toString().trim();
            }
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            KwlReturnObject rCompanyId = null;
            rCompanyId = accPurchaseOrderobj.getAllCompanyFromDb(subdomainArray);
            companyCount = rCompanyId.getRecordTotalCount();
            Iterator itrCompanyId = rCompanyId.getEntityList().iterator();
            while (itrCompanyId.hasNext()) {
                String companyid = (String) itrCompanyId.next();

                requestParams.put("companyid", companyid);
                requestParams.put("EmailID", EmailID);


                map = accScriptDao.updateMailidsLocally(requestParams);
                int cnt = (int) map.get("count");
                count+=cnt;
            }
            issuccess = true;
            txnManager.commit(status);
        } catch (Exception ex) {
            if (status != null) {
                txnManager.rollback(status);
            }
            ex.printStackTrace();
            msg = "" + ex.getMessage();
            if (ex.getMessage() == null) {
                msg = ex.getCause().getMessage();
            }
            Logger.getLogger(AccScriptController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", companyCount+" Companies Updated successfully with Mail ID"+EmailID +" in Companypreferences table and users table");

            } catch (JSONException ex) {
                Logger.getLogger(AccScriptController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    
    public ModelAndView createEntityAndProductCategory(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";        
        String responselist = "";
        KwlReturnObject kwlReturnObject = null;
        try {
            JSONObject params = StringUtil.convertRequestToJsonObject(request);
            accScriptService.createEntityAndProductCategory(params);
            issuccess = true;            
            System.out.println("DB Changes Committed...");
            msg = "Script Executed Successfully  ";
            responselist = params.optString("responselist");
        } catch (Exception ex) {            
            ex.printStackTrace();
            msg = "" + ex.getMessage();
            if (ex.getMessage() == null) {
                msg = ex.getCause().getMessage();
            }
            Logger.getLogger(AccScriptController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg + responselist);

            } catch (JSONException ex) {
                Logger.getLogger(AccScriptController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    
    public ModelAndView createRemainingCustomFields(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";        
        String responselist = "";
        KwlReturnObject kwlReturnObject = null;
        try {
            JSONObject params = StringUtil.convertRequestToJsonObject(request);
            accScriptService.createRemainingCustomFields(params);
            issuccess = true;            
            System.out.println("DB Changes Committed...");
            msg = "Script Executed Successfully  ";
            responselist = params.optString("responselist");
        } catch (Exception ex) {            
            ex.printStackTrace();
            msg = "" + ex.getMessage();
            if (ex.getMessage() == null) {
                msg = ex.getCause().getMessage();
            }
            Logger.getLogger(AccScriptController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg + responselist);

            } catch (JSONException ex) {
                Logger.getLogger(AccScriptController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }        
    /* 
     *  Script to insert enties from defaultfieldcombodata to fieldcombodata, which are not present against perticular field in defaultcustomfield
     *  (ERM-1108)
     */
    public ModelAndView insertRemainingFieldcomboData(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";        
        String responselist = "";
        try {
            JSONObject params = StringUtil.convertRequestToJsonObject(request);
            accScriptService.insertRemainingFieldcomboData(params);
            issuccess = true;            
            System.out.println("DB Changes Committed...");
            msg = "Script Executed Successfully  ";
            responselist = params.optString("responselist");
        } catch (Exception ex) {            
            ex.printStackTrace();
            msg = "" + ex.getMessage();
            if (ex.getMessage() == null) {
                msg = ex.getCause().getMessage();
            }
            Logger.getLogger(AccScriptController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg + responselist);

            } catch (JSONException ex) {
                Logger.getLogger(AccScriptController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }        
    
   public ModelAndView  DeleteEmptyValuedFieldcomboValuesMappedToEntityCustomField(HttpServletRequest request, HttpServletResponse response)
           {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        String responselist = "";
        KwlReturnObject kwlReturnObject = null;
        JSONObject params;
        try {
            params = StringUtil.convertRequestToJsonObject(request);
            accScriptService.DeleteEmptyValuedFieldcomboValuesMappedToEntityCustomField(params);


            File f = new File(storageHandlerImpl.GetDocStorePath() + "Scriptdata.zip");
            if (f.exists()) {
                response.setContentType("application/zip");
                response.setContentLength((int) f.length());
                response.addHeader("Content-Disposition", "attachment;filename=\"" + "Scriptdata.zip" + "\"");
                byte[] arBytes = new byte[32768];
                FileInputStream is = new FileInputStream(f);
                ServletOutputStream op = response.getOutputStream();
                int count;
                while ((count = is.read(arBytes)) > 0) {
                    op.write(arBytes, 0, count);
}
                op.flush();
            }

        } catch (FileNotFoundException ex) {
            Logger.getLogger(AccScriptController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(AccScriptController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ServiceException ex) {
            Logger.getLogger(AccScriptController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (JSONException ex) {
            Logger.getLogger(AccScriptController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SessionExpiredException ex) {
            Logger.getLogger(AccScriptController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
   /**
    * 
    * @param request
    * @param response
    * @return 
    */
    public ModelAndView deleteEmptyAndNoneValuesFromCustomDimension(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        JSONObject params;
        try {
            params = StringUtil.convertRequestToJsonObject(request);
            accScriptService.deleteEmptyAndNoneValuesFromCustomDimension(params);
            File f = new File(storageHandlerImpl.GetDocStorePath() + "DelDimValueScriptdata.zip");
            if (f.exists()) {
                response.setContentType("application/zip");
                response.setContentLength((int) f.length());
                response.addHeader("Content-Disposition", "attachment;filename=\"" + "DeleteDimensionScriptdata.zip" + "\"");
                byte[] arBytes = new byte[32768];
                FileInputStream is = new FileInputStream(f);
                ServletOutputStream op = response.getOutputStream();
                int count;
                while ((count = is.read(arBytes)) > 0) {
                    op.write(arBytes, 0, count);
                }
                op.flush();
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(AccScriptController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(AccScriptController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ServiceException ex) {
            Logger.getLogger(AccScriptController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (JSONException ex) {
            Logger.getLogger(AccScriptController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SessionExpiredException ex) {
            Logger.getLogger(AccScriptController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                File tempDir = new File(storageHandlerImpl.GetDocStorePath() + "DelDimValueScriptdata");
                File file2 = new File(storageHandlerImpl.GetDocStorePath() + "DelDimValueScriptdata.zip");
                if (tempDir.exists()) {
                    FileUtils.deleteDirectory(tempDir);
                    tempDir.delete();
                    System.out.println("Temp directory deleted successfully");
                }
                if (file2.exists()) {
                    FileUtils.deleteQuietly(file2);
                    System.out.println("Temp zip deleted successfully ");
                }
            } catch (IOException ex) {
                Logger.getLogger(AccScriptController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    public ModelAndView getTransactionWithTaxFromIndianCompany(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        JSONObject params;
        try {
            HSSFWorkbook wb = null;
            params = StringUtil.convertRequestToJsonObject(request);
            String fileType = request.getParameter("xls");
            wb = accScriptService.deleteTaxFromIndianCompany(params);
            exportDaoObj.writeXLSDataToFile("Tax Documents", fileType, wb, response);
            System.out.println("Script Executed Successfully");
            msg = "Script Executed Successfully  ";
            issuccess=true;
        } catch (ServiceException ex) {
            Logger.getLogger(AccScriptController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (JSONException ex) {
            Logger.getLogger(AccScriptController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SessionExpiredException ex) {
            Logger.getLogger(AccScriptController.class.getName()).log(Level.SEVERE, null, ex);
        }
        finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg );

            } catch (JSONException ex) {
                Logger.getLogger(AccScriptController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

}
