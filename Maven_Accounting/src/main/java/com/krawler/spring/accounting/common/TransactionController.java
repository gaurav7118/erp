/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.spring.accounting.common;

import com.krawler.common.session.SessionExpiredException;
import com.krawler.common.util.Constants;
import com.krawler.common.util.StringUtil;
import com.krawler.hql.accounting.invoice.service.ImportInvoiceThread;
import com.krawler.spring.accounting.goodsreceipt.service.ImportPurchaseInvoice;
import com.krawler.spring.accounting.product.accProductController;
import com.krawler.spring.authHandler.authHandler;
import com.krawler.spring.importFunctionality.ImportHandler;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.context.MessageSourceAware;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.multiaction.MultiActionController;
import org.springframework.web.servlet.support.RequestContextUtils;

/**
 *
 * @author krawler
 */
public class TransactionController extends MultiActionController {
    
    private ImportTransactionThread importTransactionThreadobj;
    public ImportHandler importHandler;
    
    public void setimportHandler(ImportHandler importHandler) {
        this.importHandler = importHandler;
    }    
    public void setimportTransactionThread(ImportTransactionThread importTransactionThreadobj) {
        this.importTransactionThreadobj = importTransactionThreadobj;
    }
    
    public ModelAndView importInvoices(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        try {
            JSONObject paramJobj = new JSONObject();
            boolean issales=true;
            String doAction = request.getParameter("do");
            if (doAction.compareToIgnoreCase("import") == 0) {
                paramJobj = getImportInvoicesParams(request);
                paramJobj.put("issales","true");
                importTransactionThreadobj.add(paramJobj);
                if (!importTransactionThreadobj.isIsworking()) {
                    Thread t = new Thread(importTransactionThreadobj);
                    t.setPriority(7);
                    t.start();
                }
                jobj.put("exceededLimit", "yes");
                jobj.put("success", true);
            } else if (doAction.compareToIgnoreCase("validateData") == 0) {
                paramJobj = getImportInvoicesParams(request);
                String eParams = paramJobj.optString("extraParams", "");
                    JSONObject extraParams = StringUtil.isNullOrEmpty(eParams) ? new JSONObject() : new JSONObject(eParams);
                    HashMap<String, Object> requestParams = importHandler.getImportRequestParams(paramJobj);
                    requestParams.put("extraParams", extraParams);
                    requestParams.put("extraObj", null);
                    requestParams.put("servletContext", paramJobj.get("servletContext"));

                    jobj = importHandler.validateFileData(requestParams);
                    jobj.put(Constants.RES_success, true);
            }
            issuccess = true;
        } catch (Exception ex) {
            issuccess = false;
            msg = "" + ex.getMessage();
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException e) {
                Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, e.getMessage(), e);
            }

            Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    
    public ModelAndView importPurchaseInvoices(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        try {
            JSONObject paramJobj = new JSONObject();
//            boolean ispurchase=true;
            String doAction = request.getParameter("do");
            if (doAction.compareToIgnoreCase("import") == 0) {
                paramJobj = getImportPurchaseInvoiceParams(request);
//                paramJobj.put("ispurchase","true");
                importTransactionThreadobj.add(paramJobj);
                if (!importTransactionThreadobj.isIsworking()) {
                    Thread t = new Thread(importTransactionThreadobj);
                    t.setPriority(7);
                    t.start();
                }
                jobj.put("exceededLimit", "yes");
                jobj.put("success", true);
            } else if (doAction.compareToIgnoreCase("validateData") == 0) {
                paramJobj = getImportPurchaseInvoiceParams(request);
                String eParams = paramJobj.optString("extraParams", "");
                JSONObject extraParams = StringUtil.isNullOrEmpty(eParams) ? new JSONObject() : new JSONObject(eParams);
                HashMap<String, Object> requestParams = importHandler.getImportRequestParams(paramJobj);
                requestParams.put("extraParams", extraParams);
                requestParams.put("extraObj", null);
                requestParams.put("servletContext", paramJobj.get("servletContext"));

                jobj = importHandler.validateFileData(requestParams);
                jobj.put("success", true);
            }
            issuccess = true;
        } catch (Exception ex) {
            issuccess = false;
            msg = "" + ex.getMessage();
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException e) {
                Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, e.getMessage(), e);
            }

            Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    
    public JSONObject getImportInvoicesParams(HttpServletRequest request) throws JSONException, SessionExpiredException {
        JSONObject paramJobj = StringUtil.convertRequestToJsonObject(request);
        paramJobj.put("servletContext", this.getServletContext());
        paramJobj.put("baseUrl", paramJobj.optString(Constants.PAGE_URL));
        return paramJobj;
    }
    
    public JSONObject getImportPurchaseInvoiceParams(HttpServletRequest request) throws JSONException, SessionExpiredException {
        JSONObject paramJobj = StringUtil.convertRequestToJsonObject(request);
        paramJobj.put("servletContext", this.getServletContext());
        paramJobj.put("baseUrl", paramJobj.optString(Constants.PAGE_URL));
        paramJobj.put("locale", RequestContextUtils.getLocale(request));
        paramJobj.put(Constants.df, authHandler.getDateOnlyFormat(request));
        return paramJobj;
    }

}
