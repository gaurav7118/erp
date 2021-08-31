/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.inventory.view;

import com.krawler.common.admin.AuditAction;
import com.krawler.common.admin.Company;
import com.krawler.common.admin.User;
import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.common.util.Paging;
import com.krawler.common.util.StringUtil;
import com.krawler.hql.accounting.AccountingException;
import com.krawler.hql.accounting.SequenceFormat;
import com.krawler.inventory.exception.SeqFormatException;
import com.krawler.inventory.model.sequence.*;
import com.krawler.spring.accounting.handler.AccountingHandlerDAO;
import com.krawler.spring.auditTrailModule.auditTrailDAO;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.spring.sessionHandler.sessionHandlerImpl;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.orm.hibernate3.HibernateTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.multiaction.MultiActionController;
import org.springframework.web.servlet.support.RequestContextUtils;

/**
 *
 * @author Vipin Gupta
 */
public class SeqController extends MultiActionController {

    private static final Logger lgr = Logger.getLogger(SeqController.class.getName());
    private HibernateTransactionManager txnManager;
    private String successView;
    private SeqService seqService;
    private AccountingHandlerDAO accountingHandlerDAO;
    private auditTrailDAO auditTrailObj;

    public void setSeqService(SeqService seqService) {
        this.seqService = seqService;
    }

    public void setAccountingHandlerDAO(AccountingHandlerDAO accountingHandlerDAO) {
        this.accountingHandlerDAO = accountingHandlerDAO;
    }

    public void setSuccessView(String successView) {
        this.successView = successView;
    }

    public void setTxnManager(HibernateTransactionManager txnManager) {
        this.txnManager = txnManager;
    }

    public void setAuditTrailObj(auditTrailDAO auditTrailObj) {
        this.auditTrailObj = auditTrailObj;
    }

    public ModelAndView getSeqModules(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("GSM_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        JSONArray jArray = new JSONArray();
        Paging paging = null;
        try {

            String companyId = sessionHandlerImpl.getCompanyid(request);

            KwlReturnObject jeresult = accountingHandlerDAO.getObject(Company.class.getName(), companyId);
            Company company = (Company) jeresult.getEntityList().get(0);

            String start = request.getParameter("start");
            String limit = request.getParameter("limit");
            paging = new Paging(start, limit);

            String searchString = request.getParameter("ss");
            String moduleName = request.getParameter("moduleName");
            if (StringUtil.isNullOrEmpty(searchString) && !StringUtil.isNullOrEmpty(moduleName)) {
                searchString = moduleName;
            }
            List<SeqModule> moduleList;
            if (!StringUtil.isNullOrEmpty(request.getParameter("isActive"))) {
                boolean isActive = Boolean.parseBoolean(request.getParameter("isActive"));
                moduleList = seqService.getSeqModules(company, isActive, searchString, paging);
            } else {
                moduleList = seqService.getSeqModules(company, searchString, paging);
            }

            for (SeqModule seqModule : moduleList) {
                JSONObject jObj = new JSONObject();
                jObj.put("id", seqModule.getId());
                jObj.put("name", seqModule.getName());
                jObj.put("isActive", seqModule.isActive());
                jArray.put(jObj);
            }
            issuccess = true;
            msg = "Seq modules has been fetched successfully";

            txnManager.commit(status);

        } catch (SessionExpiredException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            lgr.log(Level.SEVERE, msg, ex);
        } catch (SeqFormatException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            lgr.log(Level.SEVERE, msg, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
                jobj.put("data", jArray);
                if (paging != null) {
                    jobj.put("count", paging.getTotalRecord());
                } else {
                    jobj.put("count", jArray.length());
                }
            } catch (JSONException ex) {
                lgr.log(Level.SEVERE, msg, ex);
            }
        }
        return new ModelAndView(successView, "model", jobj.toString());

    }

    public ModelAndView getSeqFormats(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("GSM_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        JSONArray jArray = new JSONArray();
        Paging paging = null;
        try {

            String companyId = sessionHandlerImpl.getCompanyid(request);

            KwlReturnObject jeresult = accountingHandlerDAO.getObject(Company.class.getName(), companyId);
            Company company = (Company) jeresult.getEntityList().get(0);

            String start = request.getParameter("start");
            String limit = request.getParameter("limit");
            paging = new Paging(start, limit);
            SeqModule seqModule = null;
            String searchString = request.getParameter("ss");
            if (!StringUtil.isNullOrEmpty(request.getParameter("moduleId"))) {
                seqModule = seqService.getSeqModule(Integer.parseInt(request.getParameter("moduleId")));
            }

            List<SeqFormat> seqFormatList;
            if ("true".equalsIgnoreCase(request.getParameter("isActive"))) {
                seqFormatList = seqService.getActiveSeqFormats(company, seqModule, searchString, paging);
            } else {
                seqFormatList = seqService.getSeqFormats(company, seqModule, searchString, paging);
            }

            for (SeqFormat sf : seqFormatList) {
                JSONObject jObj = new JSONObject();
                jObj.put("seqFormatId", sf.getId());
                jObj.put("moduleId", sf.getSeqModule().getId());
                jObj.put("moduleName", sf.getSeqModule().getName());
                jObj.put("companyId", sf.getCompany().getCompanyID());
                jObj.put("prefix", sf.getPrefix());
                jObj.put("suffix", sf.getSuffix());
                jObj.put("separator", sf.getSeparator());
                jObj.put("prefixDateFormat", sf.getPrefixDateFormat() == null ? null : sf.getPrefixDateFormat().toString());
                jObj.put("suffixDateFormat", sf.getSuffixDateFormat() == null ? null : sf.getSuffixDateFormat().toString());
                jObj.put("numberOfDigits", sf.getNumberOfDigits());
                jObj.put("startFrom", sf.getStartFrom());
                jObj.put("leadingZero", true);
                jObj.put("formatedNumber", sf.getFormat());
                jObj.put("isDefault", sf.isDefaultFormat());
                jObj.put("isActive", sf.isActive());
                jArray.put(jObj);
            }
            issuccess = true;
            msg = "Seq formates have been fetched successfully";

            txnManager.commit(status);

        } catch (SessionExpiredException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            lgr.log(Level.SEVERE, msg, ex);
        } catch (SeqFormatException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            lgr.log(Level.SEVERE, msg, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
                jobj.put("data", jArray);
                if (paging != null) {
                    jobj.put("count", paging.getTotalRecord());
                } else {
                    jobj.put("count", jArray.length());
                }
            } catch (JSONException ex) {
                lgr.log(Level.SEVERE, msg, ex);
            }
        }
        return new ModelAndView(successView, "model", jobj.toString());

    }

    public ModelAndView getSeqFormatsPref(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("GSM_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        JSONArray jArray = new JSONArray();
        JSONObject jObj = new JSONObject();
        Paging paging = null;
        try {

            String companyId = sessionHandlerImpl.getCompanyid(request);

            KwlReturnObject jeresult = accountingHandlerDAO.getObject(Company.class.getName(), companyId);
            Company company = (Company) jeresult.getEntityList().get(0);

            String start = request.getParameter("start");
            String limit = request.getParameter("limit");
            paging = new Paging(start, limit);
            SeqModule seqModule = null;
            String searchString = request.getParameter("ss");
            if (!StringUtil.isNullOrEmpty(request.getParameter("moduleId"))) {
                seqModule = seqService.getSeqModule(Integer.parseInt(request.getParameter("moduleId")));
            }

            List<SeqFormat> seqFormatList;
            if ("true".equalsIgnoreCase(request.getParameter("isActive"))) {
                seqFormatList = seqService.getActiveSeqFormats(company, seqModule, searchString, paging);
            } else {
                seqFormatList = seqService.getSeqFormats(company, seqModule, searchString, paging);
            }

            String interLocation = "";
            String interStore = "";
            String StockAdjestment = "";
            String stockIssue = "";
            String stkRequest = "";
            String cycleCount= "";
            String skufiled= "";
            for (SeqFormat sf : seqFormatList) {
                String name = sf.getFormat();
                if (sf.getSeqModule().getId().equals(0)) {
                    stkRequest += name + ",";
                } else if (sf.getSeqModule().getId().equals(1)) {
                    stockIssue += name + ",";
                } else if (sf.getSeqModule().getId().equals(2)) {
                    interStore += name + ",";
                } else if (sf.getSeqModule().getId().equals(3)) {
                    StockAdjestment += name + ",";
                } else if (sf.getSeqModule().getId().equals(4)) {
                    cycleCount += name + ",";
                } else if (sf.getSeqModule().getId().equals(5)) {
                    interLocation += name + ",";
                }else if (sf.getSeqModule().getId().equals(6)) {
                    skufiled += name + ",";
                }


                jObj.put("instlocation", interLocation.endsWith(",") ? interLocation.substring(0, interLocation.length() - 1) : interLocation);
                jObj.put("inststore", interStore.endsWith(",") ? interStore.substring(0, interStore.length() - 1) : interStore);
                jObj.put("stockadj", StockAdjestment.endsWith(",") ? StockAdjestment.substring(0, StockAdjestment.length() - 1) : StockAdjestment);
                jObj.put("stkissue", stockIssue.endsWith(",") ? stockIssue.substring(0, stockIssue.length() - 1) : stockIssue);
                jObj.put("stkrequest", stkRequest.endsWith(",") ? stkRequest.substring(0, stkRequest.length() - 1) : stkRequest);
                jObj.put("cyclecount", cycleCount.endsWith(",") ? cycleCount.substring(0, cycleCount.length() - 1) : cycleCount);
                jObj.put("skufiled", skufiled.endsWith(",") ? skufiled.substring(0, skufiled.length() - 1) : skufiled);

            }
            issuccess = true;
            msg = "Seq formates have been fetched successfully";

            txnManager.commit(status);

        } catch (SessionExpiredException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            lgr.log(Level.SEVERE, msg, ex);
        } catch (SeqFormatException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            lgr.log(Level.SEVERE, msg, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
                jobj.put("data", jObj);
                if (paging != null) {
                    jobj.put("count", paging.getTotalRecord());
                } else {
                    jobj.put("count", jArray.length());
                }
            } catch (JSONException ex) {
                lgr.log(Level.SEVERE, msg, ex);
            }
        }
        return new ModelAndView(successView, "model", jobj.toString());

    }

    public ModelAndView addSeqFormat(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("GSM_Txz");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        JSONArray jArray = new JSONArray();
        String auditMessage = "";
        try {

            String userId = sessionHandlerImpl.getUserid(request);
            KwlReturnObject jeresult = accountingHandlerDAO.getObject(User.class.getName(), userId);
            User user = (User) jeresult.getEntityList().get(0);

            String prefix = "", suffix = "", separator = "";
            SeqDateFormat pdf = null, sdf = null;
            SeqModule seqModule = null;
            int numberOfDigits = 0;
            long startFrom = 0;
            if (!StringUtil.isNullOrEmpty(request.getParameter("moduleId"))) {
                seqModule = seqService.getSeqModule(Integer.parseInt(request.getParameter("moduleId")));
            }
            if (!StringUtil.isNullOrEmpty(request.getParameter("prefix"))) {
                prefix = request.getParameter("prefix");
            }
            if (!StringUtil.isNullOrEmpty(request.getParameter("prefixDateFormat"))) {
                pdf = SeqDateFormat.valueOf(request.getParameter("prefixDateFormat"));
            }
            if (!StringUtil.isNullOrEmpty(request.getParameter("suffix"))) {
                suffix = request.getParameter("suffix");
            }
            if (!StringUtil.isNullOrEmpty(request.getParameter("suffixDateFormat"))) {
                sdf = SeqDateFormat.valueOf(request.getParameter("suffixDateFormat"));
            }
            if (!StringUtil.isNullOrEmpty(request.getParameter("separator"))) {
                separator = request.getParameter("separator");
            }
            if (!StringUtil.isNullOrEmpty(request.getParameter("numberOfDigits"))) {
                numberOfDigits = Integer.parseInt(request.getParameter("numberOfDigits"));
            }
            if (!StringUtil.isNullOrEmpty(request.getParameter("startFrom"))) {
                startFrom = Long.parseLong(request.getParameter("startFrom"));
            }

            SeqFormat sf = new SeqFormat();
            sf.setCompany(user.getCompany());
            sf.setSeqModule(seqModule);
            sf.setPrefix(prefix);
            sf.setSuffix(suffix);
            sf.setPrefixDateFormat(pdf);
            sf.setSuffixDateFormat(sdf);
            sf.setSeparator(separator);
            sf.setNumberOfDigits(numberOfDigits);
            sf.setStartFrom(startFrom);
            seqService.addSeqFormat(user, sf);

            issuccess = true;
            msg = "Sequence format has been saved successfully.";

            auditMessage = "User " + user.getFullName() + " has created Sequence Format: " + sf.getFormat() + " for " + seqModule.getName();
            auditTrailObj.insertAuditLog(AuditAction.SEQ_FORMAT_INVENTORY, auditMessage, request, sf.getFormat());

            txnManager.commit(status);
        } catch (SessionExpiredException ex) {
            msg = ex.getMessage();
            lgr.log(Level.SEVERE, msg, ex);
            txnManager.rollback(status);
        } catch (SeqFormatException ex) {
            msg = ex.getMessage();
            txnManager.rollback(status);
        } catch (Exception ex) {
            msg = ex.getMessage();
            lgr.log(Level.SEVERE, msg, ex);
            txnManager.rollback(status);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                lgr.log(Level.SEVERE, msg, ex);
            }
        }
        return new ModelAndView(successView, "model", jobj.toString());

    }

    public ModelAndView previewSeqFormat(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        try {

            String userId = sessionHandlerImpl.getUserid(request);
            KwlReturnObject jeresult = accountingHandlerDAO.getObject(User.class.getName(), userId);
            User user = (User) jeresult.getEntityList().get(0);

            String prefix = "", suffix = "", separator = "";
            SeqDateFormat pdf = null, sdf = null;
            SeqModule seqModule = null;
            int numberOfDigits = 0;
            long startFrom = 0;
            if (!StringUtil.isNullOrEmpty(request.getParameter("moduleId"))) {
                seqModule = seqService.getSeqModule(Integer.parseInt(request.getParameter("moduleId")));
            }
            if (!StringUtil.isNullOrEmpty(request.getParameter("prefix"))) {
                prefix = request.getParameter("prefix");
            }
            if (!StringUtil.isNullOrEmpty(request.getParameter("prefixDateFormat"))) {
                pdf = SeqDateFormat.valueOf(request.getParameter("prefixDateFormat"));
            }
            if (!StringUtil.isNullOrEmpty(request.getParameter("suffix"))) {
                suffix = request.getParameter("suffix");
            }
            if (!StringUtil.isNullOrEmpty(request.getParameter("suffixDateFormat"))) {
                sdf = SeqDateFormat.valueOf(request.getParameter("suffixDateFormat"));
            }
            if (!StringUtil.isNullOrEmpty(request.getParameter("separator"))) {
                separator = request.getParameter("separator");
            }
            if (!StringUtil.isNullOrEmpty(request.getParameter("numberOfDigits"))) {
                numberOfDigits = Integer.parseInt(request.getParameter("numberOfDigits"));
            }
            if (!StringUtil.isNullOrEmpty(request.getParameter("startFrom"))) {
                startFrom = Long.parseLong(request.getParameter("startFrom"));
            }

            SeqFormat sf = new SeqFormat();
            sf.setCompany(user.getCompany());
            sf.setSeqModule(seqModule);
            sf.setPrefix(prefix);
            sf.setSuffix(suffix);
            sf.setPrefixDateFormat(pdf);
            sf.setSuffixDateFormat(sdf);
            sf.setSeparator(separator);
            sf.setNumberOfDigits(numberOfDigits);
            sf.setStartFrom(startFrom);

            msg = sf.getFormat();

            issuccess = true;

        } catch (SessionExpiredException ex) {
            msg = ex.getMessage();
            lgr.log(Level.SEVERE, msg, ex);
        } catch (SeqFormatException ex) {
            msg = ex.getMessage();
        } catch (Exception ex) {
            msg = ex.getMessage();
            lgr.log(Level.SEVERE, msg, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                lgr.log(Level.SEVERE, msg, ex);
            }
        }
        return new ModelAndView(successView, "model", jobj.toString());

    }

    public ModelAndView getSeqFormatNextNumber(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("GSM_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        JSONObject data = new JSONObject();
        try {

            String seqFormatId = request.getParameter("seqFormatId");
            SeqFormat seqFormat = seqService.getSeqFormat(seqFormatId);
            long seqNumber = seqService.getNextSeqNumber(seqFormat);
            String formatedSeqNumber = seqFormat.getFormat(seqNumber);
            data.put("seqNumber", seqNumber);
            data.put("formatedSeqNumber", formatedSeqNumber);

            issuccess = true;
            msg = "Next Sequence format has been fetched successfully.";

            txnManager.commit(status);

        } catch (SeqFormatException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            lgr.log(Level.SEVERE, msg, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
                jobj.put("data", data);
            } catch (JSONException ex) {
                lgr.log(Level.SEVERE, msg, ex);
            }
        }
        return new ModelAndView(successView, "model", jobj.toString());

    }

    public ModelAndView markSequenceFormatAsDefault(HttpServletRequest request, HttpServletResponse response) {

        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("GSM_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        JSONObject data = new JSONObject();
        String auditMessage = "";
        try {
            String userId = sessionHandlerImpl.getUserid(request);
            KwlReturnObject jeresult = accountingHandlerDAO.getObject(User.class.getName(), userId);
            User user = (User) jeresult.getEntityList().get(0);

            String seqFormatId = request.getParameter("seqFormatId");
            SeqFormat seqFormat = seqService.getSeqFormat(seqFormatId);
            seqService.setSeqFormatAsDefault(user, seqFormat);

            msg = "Sequence format set as default successfully.";
            issuccess = true;

            auditMessage += auditMessage = "User " + user.getFullName() + " has set Sequence Format: " + seqFormat.getFormat() + " as default for " + seqFormat.getSeqModule().getName();
            auditTrailObj.insertAuditLog(AuditAction.SEQ_FORMAT_INVENTORY, auditMessage, request, seqFormat.getId());

            txnManager.commit(status);
        } catch (SessionExpiredException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            lgr.log(Level.SEVERE, msg, ex);
        } catch (SeqFormatException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            lgr.log(Level.SEVERE, msg, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
                jobj.put("data", data);
            } catch (JSONException ex) {
                lgr.log(Level.SEVERE, msg, ex);
            }
        }
        return new ModelAndView(successView, "model", jobj.toString());

    }

    public ModelAndView activateDeactivateSequenceFormat(HttpServletRequest request, HttpServletResponse response) {

        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("GSM_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        JSONObject data = new JSONObject();
        String auditMessage = "";
        try {
            String userId = sessionHandlerImpl.getUserid(request);
            KwlReturnObject jeresult = accountingHandlerDAO.getObject(User.class.getName(), userId);
            User user = (User) jeresult.getEntityList().get(0);

            boolean deactivate = false;
            if ("true".equalsIgnoreCase(request.getParameter("deactivate"))) {
                deactivate = true;
            }
            String seqFormatId = request.getParameter("seqFormatId");
            SeqFormat seqFormat = seqService.getSeqFormat(seqFormatId);
            if (deactivate) {
                seqService.deactivateSeqFormat(user, seqFormat);
                msg = "Sequence format deactivated successfully.";
            } else {
                seqService.activateSeqFormat(user, seqFormat);
                msg = "Sequence format activated successfully.";
            }
            issuccess = true;

            auditMessage = "User " + user.getFullName() + " has " + (deactivate ? "deactivated" : "activated") + " Sequence Format: " + seqFormat.getFormat() + " for " + seqFormat.getSeqModule().getName();
            auditTrailObj.insertAuditLog(AuditAction.SEQ_FORMAT_INVENTORY, auditMessage, request, seqFormat.getFormat());

            txnManager.commit(status);
        } catch (SessionExpiredException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            lgr.log(Level.SEVERE, msg, ex);
        } catch (SeqFormatException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            lgr.log(Level.SEVERE, msg, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
                jobj.put("data", data);
            } catch (JSONException ex) {
                lgr.log(Level.SEVERE, msg, ex);
            }
        }
        return new ModelAndView(successView, "model", jobj.toString());

    }
    
    public ModelAndView deleteInvSequenceFormat(HttpServletRequest request, HttpServletResponse response) throws AccountingException {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("CAP_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        try {
            String format = request.getParameter("sequenceformat");
            String id = request.getParameter("id");
            String moduleId = request.getParameter("moduleId");
            String companyId = sessionHandlerImpl.getCompanyid(request);
            String prefix = request.getParameter("prefix");
            String prefixDateFormat = request.getParameter("prefixDateFormat");
            String suffix = request.getParameter("suffix");
            String suffixDateFormat = request.getParameter("suffixDateFormat");
            String sequenceFormatStr = "";
            String module = "";
            List list = new ArrayList();
            Map<String, Object> requestParams = new HashMap<String, Object>();
            requestParams.put("id", sessionHandlerImpl.getCompanyid(request));
            requestParams.put("companyid", companyId);
            requestParams.put("prefix", prefix);
            requestParams.put("prefixDateFormat", prefixDateFormat);
            requestParams.put("suffix", suffix);
            requestParams.put("suffixDateFormat", suffixDateFormat);
            ArrayList params = new ArrayList();
            params.add(companyId);
            if (!StringUtil.isNullOrEmpty(format)) {
                if (moduleId.equalsIgnoreCase("5") || moduleId.equalsIgnoreCase("2")) {   //'5'=Inter Location Transfer, '2'=Inter Store Transfer
                    module = "in_interstoretransfer";
                } else if (moduleId.equalsIgnoreCase("3")) {  // 3=Issue Note
                    module = "in_stockadjustment";
                }  else if (moduleId.equalsIgnoreCase("0") || moduleId.equalsIgnoreCase("1")) {  // 0=Stock Request , 1=Stock Adjustment.
                    module = "in_goodsrequest";
                }  else if (moduleId.equalsIgnoreCase("4")) {  // 4=Cycle Count
                    module = "in_cyclecount";
                }

                requestParams.put("module", module);
                list = seqService.checkInvSequenceFormat(requestParams);  // Checked whether seq fprmat is used in tramsaction or not.
                if (list.size() > 0) {
                    throw new AccountingException(" Cannot delete sequence format : used in transaction ");
                } else {
                    sequenceFormatStr = seqService.deleteInvSequenceFormatNumber(id);
                }
            }
            issuccess = true;
            txnManager.commit(status);
            jobj.put("updatedSequenceFormat", sequenceFormatStr);

        } catch (SessionExpiredException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            lgr.log(Level.SEVERE, msg, ex);
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = "" + ex.getMessage();
            lgr.log(Level.SEVERE, msg, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                lgr.log(Level.SEVERE, msg, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
}
