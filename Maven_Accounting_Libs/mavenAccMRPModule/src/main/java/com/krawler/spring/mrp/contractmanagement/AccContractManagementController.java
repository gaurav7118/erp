/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.krawler.spring.mrp.contractmanagement;
import com.krawler.common.admin.Company;
import com.krawler.common.admin.ExtraCompanyPreferences;
import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.common.util.Constants;
import com.krawler.common.util.StringUtil;
import com.krawler.esp.handlers.StorageHandler;
import com.krawler.hql.accounting.AccountingException;
import com.krawler.hql.accounting.StaticValues;
import com.krawler.spring.accounting.costCenter.CCConstants;
import com.krawler.spring.accounting.handler.AccountingHandlerDAO;
import com.krawler.spring.accounting.handler.AccountingManager;
import com.krawler.spring.authHandler.authHandler;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.spring.common.fieldDataManager;
import com.krawler.spring.sessionHandler.sessionHandlerImpl;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.io.File;
import java.net.URLDecoder;
import java.text.DateFormat;
import java.util.HashMap;
import java.util.Map;
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
public class AccContractManagementController extends MultiActionController implements MessageSourceAware{
    
    private MessageSource messageSource;
    private HibernateTransactionManager txnManager;
    private String successView;
    private AccContractManagementServiceDAO accContractManagementServiceDAOObj;
    private AccContractManagementDAO accContractManagementDAOObj;
    private AccountingHandlerDAO accountingHandlerDAOobj;
    private fieldDataManager fieldDataManagercntrl;
        
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

    public AccContractManagementServiceDAO getAccContractManagementServiceDAOObj() {
        return accContractManagementServiceDAOObj;
    }

    public void setAccContractManagementServiceDAOObj(AccContractManagementServiceDAO accContractManagementServiceDAOObj) {
        this.accContractManagementServiceDAOObj = accContractManagementServiceDAOObj;
    }

    public AccContractManagementDAO getAccContractManagementDAOObj() {
        return accContractManagementDAOObj;
    }

    public void setAccContractManagementDAOObj(AccContractManagementDAO accContractManagementDAOObj) {
        this.accContractManagementDAOObj = accContractManagementDAOObj;
    }
    
     public void setFieldDataManager(fieldDataManager fieldDataManagercntrl) {
        this.fieldDataManagercntrl = fieldDataManagercntrl;
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



    public ModelAndView getContractMasterDetails(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        try {
            
            /*Get Master Contract Related Parameters*/
             Map<String, Object> requestParams = getContractMasterCommonParameters(request);
             
            /*Get Master Contract  Details*/
             
            jobj=accContractManagementServiceDAOObj.getContractMasterDetails(requestParams);
            issuccess = true;
        } catch (Exception ex) {
            msg = "" + ex.getMessage();
            Logger.getLogger(AccContractManagementController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(AccContractManagementController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView_ex", Constants.model, jobj.toString());
    }
    
    public Map<String, Object> getContractMasterCommonParameters(HttpServletRequest request) {
        Map<String, Object> requestParams = new HashMap<>();
        try {
            String companyid = sessionHandlerImpl.getCompanyid(request);
            DateFormat df = authHandler.getDateOnlyFormat(request);
            DateFormat userdf = authHandler.getUserDateFormatter(request);
            requestParams.put(Constants.userdf, userdf);
            requestParams.put(Constants.df, df);
            requestParams.put(Constants.ss, request.getParameter(Constants.ss));
            requestParams.put(Constants.REQ_startdate, request.getParameter(Constants.REQ_startdate));
            requestParams.put(Constants.REQ_enddate, request.getParameter(Constants.REQ_enddate));
            requestParams.put("billid", request.getParameter("billid"));

            String start = request.getParameter("start");
            String limit = request.getParameter("limit");
            requestParams.put("start", start);
            requestParams.put("limit", limit);

            requestParams.put("companyid", companyid);
            requestParams.put("requestcontextutilsobj", RequestContextUtils.getLocale(request));
            
            if(!StringUtil.isNullOrEmpty(request.getParameter("moduleid"))){
                requestParams.put("moduleid", request.getParameter("moduleid"));
            }
            String searchJson = request.getParameter("searchJson");
            String filterConjuctionCriteria = request.getParameter("filterConjuctionCriteria");
            
            if(!StringUtil.isNullOrEmpty(searchJson) && !StringUtil.isNullOrEmpty(filterConjuctionCriteria)){
                requestParams.put("searchJson", request.getParameter("searchJson"));
                requestParams.put("filterConjuctionCriteria", request.getParameter("filterConjuctionCriteria"));
            }

        } catch (SessionExpiredException ex) {
            Logger.getLogger(AccContractManagementController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return requestParams;
    }



    

    

    public ModelAndView getTemporarySavedFiles(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        try {
            Map<String, Object> requestParams = new HashMap<String, Object>();
            String fileId = request.getParameter("fileid");
            String fileidStr = "";
            if (!StringUtil.isNullOrEmpty(fileId)) {
                String fileIdarr[] = fileId.split(",");
                for (int i = 0; i < fileIdarr.length; i++) {
                    fileidStr += "'" + fileIdarr[i] + "',";
                }
                if (!StringUtil.isNullOrEmpty(fileidStr)) {
                    fileidStr = fileidStr.substring(0, fileidStr.length() - 1);
                }
            }
            requestParams.put("fileid", fileidStr);
            jobj = accContractManagementServiceDAOObj.getTemporarySavedFiles(requestParams);
        } catch (Exception ex) {
            Logger.getLogger(AccContractManagementController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

     public HashMap<String, Object> getContractMap (HttpServletRequest request) throws SessionExpiredException {
        HashMap<String, Object> requestParams = AccountingManager.getGlobalParams(request);
        requestParams.put(Constants.ss, request.getParameter(Constants.ss));
        //checking filetype to print all records for csv,print & pdf
        if (StringUtil.isNullOrEmpty(request.getParameter("filetype"))) {
            requestParams.put(Constants.start, request.getParameter(Constants.start));
            requestParams.put(Constants.limit, request.getParameter(Constants.limit));
        }
        requestParams.put(Constants.df,authHandler.getDateOnlyFormat(request));
        requestParams.put(Constants.userdf,authHandler.getUserDateFormatterWithoutTimeZone(request));
        requestParams.put(CCConstants.REQ_costCenterId,request.getParameter(CCConstants.REQ_costCenterId));
        requestParams.put(Constants.REQ_customerId,request.getParameter(Constants.REQ_customerId));        
        requestParams.put(Constants.REQ_startdate ,request.getParameter(Constants.REQ_startdate));
        requestParams.put(Constants.REQ_enddate ,request.getParameter(Constants.REQ_enddate));
        requestParams.put(Constants.MARKED_FAVOURITE ,request.getParameter(Constants.MARKED_FAVOURITE));
        requestParams.put("newcustomerid", request.getParameter("newcustomerid"));
        requestParams.put("productid", request.getParameter("productid"));
        requestParams.put("productCategoryid", request.getParameter("productCategoryid"));
        requestParams.put(Constants.isRepeatedFlag, request.getParameter(Constants.isRepeatedFlag));
        requestParams.put("userid", request.getParameter("userid"));//putting userid to export number of users
        requestParams.put("deleted", request.getParameter("deleted"));
        requestParams.put("nondeleted", request.getParameter("nondeleted"));
        requestParams.put("companyid", sessionHandlerImpl.getCompanyid(request));
        requestParams.put("orderforcontract", request.getParameter("orderForContract")!=null?Boolean.parseBoolean(request.getParameter("orderForContract")):false);
        requestParams.put(Constants.ValidFlag, request.getParameter(Constants.ValidFlag));
        requestParams.put(Constants.BillDate ,request.getParameter(Constants.BillDate));
        requestParams.put("pendingapproval" ,(request.getParameter("pendingapproval") != null)? Boolean.parseBoolean(request.getParameter("pendingapproval")): false);
        requestParams.put("linkflag" ,request.getParameter("linkflag"));
        requestParams.put(Constants.Acc_Search_Json ,request.getParameter(Constants.Acc_Search_Json));
        requestParams.put(Constants.Filter_Criteria ,request.getParameter(Constants.Filter_Criteria));
        requestParams.put(Constants.moduleid ,request.getParameter(Constants.moduleid));
        requestParams.put("currencyfilterfortrans", (request.getParameter("currencyfilterfortrans") == null)? "" : request.getParameter("currencyfilterfortrans"));
        requestParams.put("isOpeningBalanceOrder", request.getParameter("isOpeningBalanceOrder")!=null?Boolean.parseBoolean(request.getParameter("isOpeningBalanceOrder")):false);
        requestParams.put("isLeaseFixedAsset", request.getParameter("isLeaseFixedAsset")!=null?Boolean.parseBoolean(request.getParameter("isLeaseFixedAsset")):false);
        requestParams.put("isConsignment", request.getParameter("isConsignment")!=null?Boolean.parseBoolean(request.getParameter("isConsignment")):false);
        requestParams.put(CCConstants.REQ_customerId,request.getParameter(CCConstants.REQ_customerId));
        requestParams.put(Constants.customerCategoryid, request.getParameter(Constants.customerCategoryid));
        requestParams.put("billId",request.getParameter("billid"));
        requestParams.put("blockedDocuments",request.getParameter("blockedDocuments"));
        requestParams.put("unblockedDocuments",request.getParameter("unblockedDocuments"));
        requestParams.put(Constants.checksoforcustomer,StringUtil.isNullOrEmpty(request.getParameter(Constants.checksoforcustomer)) ? false : Boolean.parseBoolean(request.getParameter(Constants.checksoforcustomer)));
        if(request.getParameter("requestModuleid")!=null){
            requestParams.put("requestModuleid",Integer.parseInt(request.getParameter("requestModuleid")));
        }
        requestParams.put("isDraft", (request.getParameter("isDraft") != null) ? Boolean.parseBoolean(request.getParameter("isDraft")) : false);    
        return requestParams;
    }
     
    public ModelAndView getMasterContracts(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj=new JSONObject();
        String msg = "";
        boolean issuccess = false;
        try {
            HashMap<String, Object> requestParams = getContractMap(request);
             KwlReturnObject extracapresult = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), sessionHandlerImpl.getCompanyid(request));
            ExtraCompanyPreferences extraCompanyPreferences = (ExtraCompanyPreferences) extracapresult.getEntityList().get(0);
            if (request.getParameter("requestModuleid") != null && !StringUtil.isNullOrEmpty(request.getParameter("requestModuleid"))) {
                int requestModuleID=Integer.parseInt(request.getParameter("requestModuleid"));
                if(extraCompanyPreferences.isEnableLinkToSelWin()){
                    requestParams.put("requestModuleid", requestModuleID);    
                }
                if(extraCompanyPreferences.isEnableLinkToSelWin() && !Boolean.parseBoolean(request.getParameter("isGrid"))){
                     requestParams.put("start","0");
                     requestParams.put("limit", "10");
                     requestParams.put("dropDown", true);
                }
            }
            jobj= accContractManagementServiceDAOObj.getMasterContracts(requestParams);
            issuccess = true;
        } catch (SessionExpiredException | ServiceException | NumberFormatException ex) {
            msg = "" + ex.getMessage();
            Logger.getLogger(AccContractManagementController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(AccContractManagementController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    
    public ModelAndView deleteMasterContracts(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("Machine_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = null;
        String msg = "";
        try {
            status = txnManager.getTransaction(def);
            Map<String, Object> requestParams = new HashMap<>();
            String[] arrayOfID = null;
            String companyid = sessionHandlerImpl.getCompanyid(request);

            if (!StringUtil.isNullOrEmpty(request.getParameter("isdelete")) && request.getParameter("isdelete").equalsIgnoreCase("true")) {
                JSONArray jArr = new JSONArray(request.getParameter("data"));
                arrayOfID = new String[jArr.length()];
                for (int i = 0; i < jArr.length(); i++) {
                    JSONObject masterContractID = jArr.getJSONObject(i);
                    arrayOfID[i] = StringUtil.DecodeText(masterContractID.optString("id"));
                }
                requestParams.put("idsfordelete", arrayOfID);
            }
            requestParams.put("companyid", companyid);

            jobj = accContractManagementServiceDAOObj.deleteMasterContracts(requestParams);
            txnManager.commit(status);
             msg = messageSource.getMessage("mrp.mastercontract.delete.success.msg", null, RequestContextUtils.getLocale(request));
            issuccess = true;
        } catch (Exception ex) {
            if (status != null) {
                txnManager.rollback(status);
            }
            msg = messageSource.getMessage("mrp.mastercontract.delete.failure.msg", null, RequestContextUtils.getLocale(request));
            Logger.getLogger(AccContractManagementController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(AccContractManagementController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", Constants.model, jobj.toString());
    }
    
    public ModelAndView deleteMasterContractsPermanently(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("Machine_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = null;
        String msg = "";
        try {
            status = txnManager.getTransaction(def);
            Map<String, Object> requestParams = new HashMap<>();
            String[] arrayOfID = null;
            String companyid = sessionHandlerImpl.getCompanyid(request);

            if (!StringUtil.isNullOrEmpty(request.getParameter("isdelete")) && request.getParameter("isdelete").equalsIgnoreCase("true")) {
                JSONArray jArr = new JSONArray(request.getParameter("data"));
                arrayOfID = new String[jArr.length()];
                for (int i = 0; i < jArr.length(); i++) {
                    JSONObject masterContractID = jArr.getJSONObject(i);
                    arrayOfID[i] = StringUtil.DecodeText(masterContractID.optString("id"));
                }
                requestParams.put("idsfordelete", arrayOfID);
            }
            requestParams.put("companyid", companyid);
            
            jobj = accContractManagementServiceDAOObj.deleteMasterContractsPermanently(requestParams);
            
            txnManager.commit(status);
            issuccess = true;
            msg = jobj.getString("msg");
        } catch (Exception ex) {
            if (status != null) {
                txnManager.rollback(status);
            }
            msg = "" + ex.getMessage();
            Logger.getLogger(AccContractManagementController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(AccContractManagementController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", Constants.model, jobj.toString());
    }
    
    public ModelAndView exportContractMaster(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        try {
            
            /*Get Contract Master Related Parameters*/
             Map<String, Object> requestParams = getContractMap(request);
             requestParams.put("request", request);
             requestParams.put("response", response);
             
             /*Get Contract Details*/
             
             jobj=accContractManagementServiceDAOObj.exportContractMaster(requestParams);
            issuccess = true;
        } catch (Exception ex) {
            msg = "" + ex.getMessage();
            Logger.getLogger(AccContractManagementController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(AccContractManagementController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", Constants.model, jobj.toString());
    }
    
    public ModelAndView getMasterContractRows(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        try {
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            requestParams.put("mrpcontractid", request.getParameter("mrpcontractid"));
            
            jobj = accContractManagementServiceDAOObj.getMasterContractRows(requestParams);
            issuccess = true;
        } catch (Exception ex) {
            msg = "" + ex.getMessage();
            Logger.getLogger(AccContractManagementController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(AccContractManagementController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
       
}
