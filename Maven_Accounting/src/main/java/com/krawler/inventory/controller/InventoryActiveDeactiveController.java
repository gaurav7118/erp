/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.inventory.controller;

import com.krawler.inventory.model.activation.RunnableActivateInventory;
import com.krawler.common.admin.Company;
import com.krawler.common.admin.User;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.common.util.Paging;
import com.krawler.inventory.exception.InventoryException;
import com.krawler.inventory.model.activation.InventoryActivationService;
import com.krawler.inventory.model.activation.RunnableDeactivateInventory;
import com.krawler.spring.accounting.handler.AccountingHandlerDAO;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.spring.sessionHandler.sessionHandlerImpl;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
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

/**
 *
 * @author Vipin Gupta
 */
public class InventoryActiveDeactiveController extends MultiActionController {

    private static final Logger lgr = Logger.getLogger(InventoryActiveDeactiveController.class.getName());
    private HibernateTransactionManager txnManager;
    private String successView;
    private AccountingHandlerDAO accountingHandlerDAO;
    private InventoryActivationService activationService;

    public void setTxnManager(HibernateTransactionManager txnManager) {
        this.txnManager = txnManager;
    }

    public void setSuccessView(String successView) {
        this.successView = successView;
    }

    public void setAccountingHandlerDAO(AccountingHandlerDAO accountingHandlerDAO) {
        this.accountingHandlerDAO = accountingHandlerDAO;
    }

    public void setActivationService(InventoryActivationService activationService) {
        this.activationService = activationService;
    }


    public ModelAndView getInactivedInventoryProducts(HttpServletRequest request, HttpServletResponse response) {

        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("IST_Tx");
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


            List<Object[]> products = activationService.getInvactivatedInventoryProductsForUI(company, searchString, paging);

            for (Object[] p : products) {
                JSONObject jObj = new JSONObject();
                jObj.put("id", p[0]);
                jObj.put("productid", p[1]);
                jObj.put("productname", p[2]);
                jObj.put("desc", p[3]);

                jArray.put(jObj);
            }

            issuccess = true;
            msg = "Product list has been fetched successfully";

            txnManager.commit(status);

        } catch (SessionExpiredException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            lgr.log(Level.SEVERE, msg, ex);
        } catch (InventoryException ex) {
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

    public ModelAndView activateInventory(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        try {
            String userId = sessionHandlerImpl.getUserid(request);
            KwlReturnObject jeresult = accountingHandlerDAO.getObject(User.class.getName(), userId);
            User user = (User) jeresult.getEntityList().get(0);

            boolean processAlreadyRunning = activationService.isActivateDeactivateProcessRunning(user.getCompany());
            if (!processAlreadyRunning) {
                String setupData = request.getParameter("setupData");
                JSONObject setupJObj = new JSONObject(setupData);

                Map auditParams = new HashMap();
                auditParams.put("reqHeader", request.getHeader("x-real-ip"));
                auditParams.put("remoteAddress", request.getRemoteAddr());
                auditParams.put("userid", sessionHandlerImpl.getUserid(request));
                auditParams.put("userFullName", sessionHandlerImpl.getUserFullName(request));

                RunnableActivateInventory rai = new RunnableActivateInventory(setupJObj, activationService, user, auditParams);
                Thread thread = new Thread(rai);
                thread.start();

                issuccess = true;
            } else {
                issuccess = false;
                msg = "Already Processing for Activate/Deactivate inventory ";
            }

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
    
    public ModelAndView getAllInTransitTransactions(HttpServletRequest request, HttpServletResponse response) {

        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("IST_Tx");
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


            List<Object[]> inTransitTransactions = activationService.getAllInTransitTransactionRequests(company, paging);

            for (Object[] t : inTransitTransactions) {
                JSONObject jObj = new JSONObject();
                jObj.put("transactionNo", t[0]);
                jObj.put("product", t[1]);
                jObj.put("module", t[2]);

                jArray.put(jObj);
            }

            issuccess = true;
            msg = "transaction list has been fetched successfully";

            txnManager.commit(status);

        } catch (SessionExpiredException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            lgr.log(Level.SEVERE, msg, ex);
        } catch (InventoryException ex) {
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
    
    public ModelAndView deactivateInventory(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        try {
            String userId = sessionHandlerImpl.getUserid(request);
            KwlReturnObject jeresult = accountingHandlerDAO.getObject(User.class.getName(), userId);
            User user = (User) jeresult.getEntityList().get(0);

            boolean processAlreadyRunning = activationService.isActivateDeactivateProcessRunning(user.getCompany());
            if (!processAlreadyRunning) {

                Map auditParams = new HashMap();
                auditParams.put("reqHeader", request.getHeader("x-real-ip"));
                auditParams.put("remoteAddress", request.getRemoteAddr());
                auditParams.put("userid", sessionHandlerImpl.getUserid(request));
                auditParams.put("userFullName", sessionHandlerImpl.getUserFullName(request));

                RunnableDeactivateInventory rdi = new RunnableDeactivateInventory(activationService, user, auditParams);
                Thread thread = new Thread(rdi);
                thread.start();

                issuccess = true;
            } else {
                issuccess = false;
                msg = "Already Processing for Activate/Deactivate inventory ";
            }

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
    
}
