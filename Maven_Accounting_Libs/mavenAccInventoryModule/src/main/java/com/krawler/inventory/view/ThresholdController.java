/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.inventory.view;

import com.krawler.common.admin.AuditAction;
import com.krawler.common.admin.Company;
import com.krawler.common.admin.User;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.common.util.Paging;
import com.krawler.common.util.StringUtil;
import com.krawler.hql.accounting.Product;
import com.krawler.inventory.exception.InventoryException;
import com.krawler.inventory.model.cyclecount.*;
import com.krawler.inventory.model.sequence.SeqService;
import com.krawler.inventory.model.store.Store;
import com.krawler.inventory.model.store.StoreService;
import com.krawler.inventory.model.threshold.ProductThreshold;
import com.krawler.inventory.model.threshold.ThresholdException;
import com.krawler.inventory.model.threshold.ThresholdService;
import com.krawler.spring.accounting.handler.AccountingHandlerDAO;
import com.krawler.spring.auditTrailModule.auditTrailDAO;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.spring.sessionHandler.sessionHandlerImpl;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.context.MessageSource;
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
public class ThresholdController extends MultiActionController {

    private static final Logger lgr = Logger.getLogger(ThresholdController.class.getName());
    private HibernateTransactionManager txnManager;
    private String successView;
    private StoreService storeService;
    private ThresholdService thresholdService;
    private AccountingHandlerDAO accountingHandlerDAO;
    private auditTrailDAO auditTrailObj;
    private MessageSource messageSource;

    public void setAccountingHandlerDAO(AccountingHandlerDAO accountingHandlerDAO) {
        this.accountingHandlerDAO = accountingHandlerDAO;
    }

    public void setStoreService(StoreService storeService) {
        this.storeService = storeService;
    }

    public void setThresholdService(ThresholdService thresholdService) {
        this.thresholdService = thresholdService;
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

    public void setMessageSource(MessageSource ms) {
        this.messageSource = ms;
    }
    
    public ModelAndView updateProductThreshold(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean isSuccess = false;
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("GTR_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        String auditMessage = "";
        try {
            String userId = sessionHandlerImpl.getUserid(request);
            KwlReturnObject jeresult = accountingHandlerDAO.getObject(User.class.getName(), userId);
            User user = (User) jeresult.getEntityList().get(0);

            JSONArray jArr = new JSONArray(request.getParameter("thresholdDataArray"));
            for (int i = 0; i < jArr.length(); i++) {
                double oldThresholdLimit = 0;

                JSONObject jobj1 = jArr.getJSONObject(i);
                String storeId = jobj1.getString("storeId");
                String productId = jobj1.getString("productId");
                double thresholdLimit = jobj1.optDouble("thresholdLimit", 0);
                Store store = storeService.getStoreById(storeId);
                jeresult = accountingHandlerDAO.getObject(Product.class.getName(), productId);
                Product product = (Product) jeresult.getEntityList().get(0);

                ProductThreshold oldThreshold = thresholdService.getProductThreshold(product, store);
                if (oldThreshold != null) {
                    oldThresholdLimit = oldThreshold.getThresholdLimit();
                }
                
                thresholdService.addOrUpdateProductThreshold(product, store, thresholdLimit);
                
                if (oldThresholdLimit != thresholdLimit) {
                    if (!StringUtil.isNullOrEmpty(auditMessage)) {
                        auditMessage += ", ";
                    }
                    auditMessage += "(Product: " + product.getProductid() + ", Store: " + store.getAbbreviation() + ", Threshold Limit from(" + oldThresholdLimit + " to " + thresholdLimit+") )";
                }
            }
            isSuccess = true;
            msg = messageSource.getMessage("acc.product.threshold.limit.updated.successfully", null, RequestContextUtils.getLocale(request));
            
            auditMessage = "User " + user.getFullName() + " has updated Product Threshold Limit: " + auditMessage;
            auditTrailObj.insertAuditLog(AuditAction.PRODUCT_THRESHOLD, auditMessage, request, "0");

            txnManager.commit(status);
        } catch (ThresholdException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            lgr.log(Level.SEVERE, msg, ex);
        } finally {
            try {
                jobj.put("success", isSuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                lgr.log(Level.SEVERE, msg, ex);
            }
        }
        return new ModelAndView(successView, "model", jobj.toString());
    }

    public ModelAndView getStoreWiseThresholdList(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean isSuccess = false;
        JSONArray jArray = new JSONArray();
        Paging paging = null;
        try {
            String companyId = sessionHandlerImpl.getCompanyid(request);
            KwlReturnObject jeresult = accountingHandlerDAO.getObject(Company.class.getName(), companyId);
            Company company = (Company) jeresult.getEntityList().get(0);

            String storeId = request.getParameter("storeId");
            String ss = request.getParameter("ss");
            String start = request.getParameter("start");
            String limit = request.getParameter("limit");

            paging = new Paging(start, limit);

            Store store = null;
            if (!StringUtil.isNullOrEmpty(storeId)) {
                store = storeService.getStoreById(storeId);
            }
            List<ProductThreshold> ptList = thresholdService.getStoreWiseThresholdList(store, ss, paging);

            for (ProductThreshold pt : ptList) {
                JSONObject jObj = new JSONObject();

                jObj.put("storeId", storeId);
                jObj.put("storeCode", store.getAbbreviation());
                jObj.put("storeName", store.getDescription());
                jObj.put("productId", pt.getProduct().getID());
                jObj.put("productCode", pt.getProduct().getProductid());
                jObj.put("productName", pt.getProduct().getName());
                jObj.put("thresholdLimit", pt.getThresholdLimit());

                jArray.put(jObj);
            }

            isSuccess = true;
            msg = "Product Threshold has been fetched successfully";

        } catch (ThresholdException ex) {
            msg = ex.getMessage();
        } catch (Exception ex) {
            msg = ex.getMessage();
            lgr.log(Level.SEVERE, msg, ex);
        } finally {
            try {
                jobj.put("success", isSuccess);
                jobj.put("msg", msg);
                jobj.put("data", jArray);
                jobj.put("count", paging.getTotalRecord());
            } catch (JSONException ex) {
                lgr.log(Level.SEVERE, msg, ex);
            }
        }
        return new ModelAndView(successView, "model", jobj.toString());
    }

    public ModelAndView getProductWiseThresholdList(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean isSuccess = false;
        JSONArray jArray = new JSONArray();
        Paging paging = null;
        try {
            String companyId = sessionHandlerImpl.getCompanyid(request);
            KwlReturnObject jeresult = accountingHandlerDAO.getObject(Company.class.getName(), companyId);
            Company company = (Company) jeresult.getEntityList().get(0);

            String productId = request.getParameter("productId");
            String ss = request.getParameter("ss");
            String start = request.getParameter("start");
            String limit = request.getParameter("limit");

            paging = new Paging(start, limit);


            Product product = null;
            if (!StringUtil.isNullOrEmpty(productId)) {
                jeresult = accountingHandlerDAO.getObject(Product.class.getName(), productId);
                product = (Product) jeresult.getEntityList().get(0);
            }
            List<ProductThreshold> ptList = thresholdService.getProductWiseThresholdList(product, start, paging);

            for (ProductThreshold pt : ptList) {
                JSONObject jObj = new JSONObject();

                jObj.put("storeId", pt.getStore().getId());
                jObj.put("storeCode", pt.getStore().getAbbreviation());
                jObj.put("storeName", pt.getStore().getDescription());
                jObj.put("productId", productId);
                jObj.put("productCode", product.getProductid());
                jObj.put("productName", product.getName());
                jObj.put("thresholdLimit", pt.getThresholdLimit());

                jArray.put(jObj);
            }

            isSuccess = true;
            msg = "Product Threshold has been fetched successfully";

        } catch (Exception ex) {
            msg = ex.getMessage();
            lgr.log(Level.SEVERE, msg, ex);
        } finally {
            try {
                jobj.put("success", isSuccess);
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

    public ModelAndView getThresholdStockReport(HttpServletRequest request, HttpServletResponse response) {

        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("GTR_Tx");
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
            String storeId = request.getParameter("storeId");
            Store store = null;

            if (!StringUtil.isNullOrEmpty(storeId)) {
                store = storeService.getStoreById(storeId);
            }
            List<Map<String, Object>> thresholdStockList = thresholdService.getThresholdStockList(company, store, searchString, paging);

            for (Map<String, Object> dataMap : thresholdStockList) {
                String productId = (String) dataMap.get("productId");
                String productCode = (String) dataMap.get("productCode");
                String productName = (String) dataMap.get("productName");
                double thresholdLimit = (Double) dataMap.get("thresholdLimit");
                double stockInHand = (Double) dataMap.get("inhandQuantity");
                JSONObject jObj = new JSONObject();
                jObj.put("storeId", storeId);
                jObj.put("storeCode", store.getAbbreviation());
                jObj.put("storeName", store.getDescription());
                jObj.put("productId", productId);
                jObj.put("productCode", productCode);
                jObj.put("productName", productName);
                jObj.put("thresholdLimit", thresholdLimit);
                jObj.put("stockInHand", stockInHand);

                jArray.put(jObj);
            }
            issuccess = true;
            msg = "Threshold Report fetched successfully";

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
}
