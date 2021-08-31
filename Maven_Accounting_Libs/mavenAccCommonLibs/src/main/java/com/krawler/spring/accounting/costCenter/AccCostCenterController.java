/*
 * Copyright (C) 2012  Krawler Information Systems Pvt Ltd
 * All rights reserved.
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package com.krawler.spring.accounting.costCenter;

import com.krawler.common.admin.CostCenter;
import com.krawler.common.admin.SalesCommission;
import com.krawler.common.admin.ApprovalRules;
import com.krawler.common.service.ServiceException;
import com.krawler.common.util.Constants;
import com.krawler.common.util.StringUtil;
import com.krawler.spring.accounting.handler.AccountingManager;
import com.krawler.spring.auditTrailModule.auditTrailDAO;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.spring.sessionHandler.sessionHandlerImpl;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceAware;
import org.springframework.orm.hibernate3.HibernateTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.multiaction.MultiActionController;
import org.springframework.web.servlet.support.RequestContextUtils;
import com.krawler.common.admin.AuditAction;
import com.krawler.spring.accounting.costCenter.service.AccCostCenterService;

/**
 *
 * @author krawler
 */
public class AccCostCenterController extends MultiActionController implements MessageSourceAware {

    private HibernateTransactionManager txnManager;
    private AccCostCenterDAO accCostCenterObj;
    private String successView;
    private MessageSource messageSource;
    private auditTrailDAO auditTrailObj;
    private AccCostCenterService accCostCenterService;
    
    @Override
    public void setMessageSource(MessageSource ms) {
        this.messageSource = ms;
    }

    public void setAccCostCenterService(AccCostCenterService accCostCenterService) {
        this.accCostCenterService = accCostCenterService;
    }
     
    public void setTxnManager(HibernateTransactionManager txManager) {
        this.txnManager = txManager;
    }

    public void setaccCostCenterDAO(AccCostCenterDAO accCostCenterDAOObj) {
        this.accCostCenterObj = accCostCenterDAOObj;
    }

    public String getSuccessView() {
        return successView;
    }

    public void setSuccessView(String successView) {
        this.successView = successView;
    }

    public void setauditTrailDAO(auditTrailDAO auditTrailDAOObj) {
        this.auditTrailObj = auditTrailDAOObj;
    }

    public ModelAndView getCostCenter(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = null;
        try {
            HashMap<String, Object> requestParams = AccountingManager.getGlobalParams(request);
            String start = request.getParameter(Constants.start);
            String limit = request.getParameter(Constants.limit);
            
            if (request.getParameter("query") != null && !StringUtil.isNullOrEmpty(request.getParameter("query"))) {
                String ss = (String) request.getParameter("query");
                requestParams.put(Constants.ss, ss);
                requestParams.put("ss_names", new String[]{"name"});
            }

            ArrayList filter_names = new ArrayList(), filter_params = new ArrayList();
            filter_names.add("company.companyID");
            filter_params.add(requestParams.get(Constants.companyKey));
            requestParams.put(Constants.filterNamesKey, filter_names);
            requestParams.put(Constants.filterParamsKey, filter_params);

            KwlReturnObject result = accCostCenterObj.getCostCenter(requestParams);
            List<CostCenter> list = result.getEntityList();
            int count = result.getRecordTotalCount();

            JSONArray DataJArr = getCostCenterJson(request, list);
            JSONArray pagedJson = DataJArr;
            if (!StringUtil.isNullOrEmpty(start) && !StringUtil.isNullOrEmpty(limit)) {
                pagedJson = StringUtil.getPagedJSON(pagedJson, Integer.parseInt(start), Integer.parseInt(limit));
            }
            jobj.put(Constants.RES_data, pagedJson);
            jobj.put(Constants.RES_count, DataJArr.length());
            issuccess = true;
        } catch (Exception ex) {
            msg = ex.getMessage();
        } finally {
            try {
                jobj.put(Constants.RES_success, issuccess);
                jobj.put(Constants.RES_msg, msg == null ? "null" : msg);
            } catch (JSONException ex) {
                Logger.getLogger(AccCostCenterController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView(Constants.jsonView, Constants.model, jobj.toString());
    }

    public JSONArray getCostCenterJson(HttpServletRequest request, List<CostCenter> costCenters) throws ServiceException {
            JSONArray jArr = new JSONArray();
        try {
            String forCombo = request.getParameter(CCConstants.REQ_FORCOMBO) == null ? "" : request.getParameter(CCConstants.REQ_FORCOMBO);
            boolean isquery= !StringUtil.isNullOrEmpty(request.getParameter("query"))?true:false;
            
            boolean isForReport= !StringUtil.isNullOrEmpty(request.getParameter("isForReport"))?Boolean.parseBoolean(request.getParameter("isForReport")):false;
            if (!costCenters.isEmpty() && !StringUtil.isNullOrEmpty(forCombo) && !isquery && !isForReport) {
                JSONObject obj = new JSONObject();
                obj.put(CCConstants.JSON_ID, "");
                obj.put(CCConstants.JSON_CCID, "");
                if (!forCombo.equalsIgnoreCase(CCConstants.REQ_COMBO_REPORT)) {
                    obj.put(CCConstants.JSON_NAME, messageSource.getMessage("acc.rem.111", null, RequestContextUtils.getLocale(request)));
                } else {
                    obj.put(CCConstants.JSON_NAME, forCombo.equalsIgnoreCase(CCConstants.REQ_COMBO_REPORT) ? messageSource.getMessage("acc.rem.110", null, RequestContextUtils.getLocale(request)) : messageSource.getMessage("acc.rem.111", null, RequestContextUtils.getLocale(request)));
                }
                obj.put(CCConstants.JSON_DESC, "");
                jArr.put(obj);
            }

            if (costCenters != null && !costCenters.isEmpty()) {
                for (CostCenter costCenter : costCenters) {
                    JSONObject obj = new JSONObject();
                    obj.put(CCConstants.JSON_ID, costCenter.getID());
                    obj.put(CCConstants.JSON_CCID, costCenter.getCcid());
                    obj.put(CCConstants.JSON_NAME, costCenter.getName());
                    obj.put(CCConstants.JSON_DESC, costCenter.getDescription());
                    jArr.put(obj);
                }
            }
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("getCostCenterJson : " + ex.getMessage(), ex);
        }
        return jArr;
    }

    public ModelAndView saveCostCenter(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        try {
            JSONObject paramJobj = StringUtil.convertRequestToJsonObject(request);
            jobj = accCostCenterService.saveCostCenter(paramJobj);
        } catch (Exception ex) {
            msg = "" + ex.getMessage();
            Logger.getLogger(AccCostCenterController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new ModelAndView(Constants.jsonView, Constants.model, jobj.toString());
    }

    public ModelAndView getSalesCommission(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = null;
        try {
            HashMap<String, Object> requestParams = AccountingManager.getGlobalParams(request);

            ArrayList filter_names = new ArrayList(), filter_params = new ArrayList();
            filter_names.add("company.companyID");
            filter_params.add(requestParams.get(Constants.companyKey));
            requestParams.put(Constants.filterNamesKey, filter_names);
            requestParams.put(Constants.filterParamsKey, filter_params);

            KwlReturnObject result = accCostCenterObj.getSalesCommission(requestParams);
            List<SalesCommission> list = result.getEntityList();
            int count = result.getRecordTotalCount();

            JSONArray jSONArray = new JSONArray();
            for (SalesCommission salesCommission : list) {
                JSONObject jSONObject = new JSONObject();
                jSONObject.put("id", salesCommission.getID());
                jSONObject.put("commission", salesCommission.getCommission());
                jSONArray.put(jSONObject);
            }
            jobj.put(Constants.RES_data, jSONArray);
            jobj.put(Constants.RES_count, count);
            issuccess = true;
        } catch (Exception ex) {
            msg = ex.getMessage();
        } finally {
            try {
                jobj.put(Constants.RES_success, issuccess);
                jobj.put(Constants.RES_msg, msg == null ? "null" : msg);
            } catch (JSONException ex) {
                Logger.getLogger(AccCostCenterController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView(Constants.jsonView, Constants.model, jobj.toString());
    }

//    public ModelAndView saveSalesCommission(HttpServletRequest request, HttpServletResponse response) {
//        JSONObject jobj = new JSONObject();
//        String msg = "";
//        boolean issuccess = false, isCommitEx = false;
//
//        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
//        def.setName("CCenter_Tx");
//        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
//
//        TransactionStatus status = txnManager.getTransaction(def);
//        try {
//            saveSalesCommission(request);
//            issuccess = true;
//            msg = "Sales Commission has been saved successfully.";  //CCConstants.CC_SUCCESS_MSG;
//
//            try {
//                txnManager.commit(status);
//            } catch (Exception ex) {
//                isCommitEx = true;
//                msg = ex.getMessage();
//            }
//        } catch (Exception ex) {
//            if (!isCommitEx) {
//                txnManager.rollback(status);
//            }
//            msg = "" + ex.getMessage();
//            Logger.getLogger(AccCostCenterController.class.getName()).log(Level.SEVERE, null, ex);
//        } finally {
//            try {
//                jobj.put(Constants.RES_success, issuccess);
//                jobj.put(Constants.RES_msg, msg);
//            } catch (JSONException ex) {
//                Logger.getLogger(AccCostCenterController.class.getName()).log(Level.SEVERE, null, ex);
//            }
//        }
//        return new ModelAndView(Constants.jsonView, Constants.model, jobj.toString());
//    }
//
//    public void saveSalesCommission(HttpServletRequest request) throws ServiceException, AccountingException, SessionExpiredException {
//        HashMap<String, Object> ccMap;
//        try {
//            String salesCommissionStr = StringUtil.isNullOrEmpty("salescommission") ? "" : request.getParameter("salescommission");
//            String companyid = sessionHandlerImpl.getCompanyid(request);
//
//            if (!StringUtil.isNullOrEmpty(salesCommissionStr)) {
//                accCostCenterObj.deleteSalesCommission(companyid);
//
//                double commission = (Double) Double.parseDouble(salesCommissionStr);
//
//                ccMap = new HashMap<String, Object>();
//                ccMap.put("Commission", commission);
//                ccMap.put("Company", companyid);
//
//                accCostCenterObj.saveSalesCommission(ccMap);
//                auditTrailObj.insertAuditLog(AuditAction.SALES_COMMISSION_ADDED, "User " + sessionHandlerImpl.getUserFullName(request) + " has set Sales Commission " + salesCommissionStr + "%", request, companyid);
//            }
//        } catch (Exception ex) {
//            throw ServiceException.FAILURE(ex.getMessage(), ex);
//        }
//    }

    public ModelAndView saveApprovalRules(HttpServletRequest request, HttpServletResponse response) throws ServiceException {

        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("CCenter_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);

        TransactionStatus status = txnManager.getTransaction(def);

        JSONObject jobj = new JSONObject();
        String id = UUID.randomUUID().toString();
//        String result="";
        KwlReturnObject result = null;
        try {
            String companyid = request.getParameter("companyid");
            String trasactionType = request.getParameter("trasactiontype");
            String fieldType = request.getParameter("fieldtype");


            JSONObject duplicatejObj = getApprovalRulesForDuplicate(trasactionType, fieldType, companyid);


            HashMap<String, Object> approvalRulesMap = new HashMap<String, Object>();
            approvalRulesMap.put("ID", id);
            approvalRulesMap.put("RuleName", request.getParameter("rulename"));
            approvalRulesMap.put("Typeid", trasactionType);
            approvalRulesMap.put("FieldType", fieldType);
            approvalRulesMap.put("Value", request.getParameter("value"));
            if (!StringUtil.isNullOrEmpty(request.getParameter("discountamount"))) {
                approvalRulesMap.put("discount", request.getParameter("discountamount"));
            }
            approvalRulesMap.put("approvallevel", Integer.parseInt(request.getParameter("approvallevel")));
            approvalRulesMap.put("Company", companyid);

            if (!duplicatejObj.getBoolean("duplicate")) {
                result = accCostCenterObj.saveApprovalRules(approvalRulesMap);
                if (result.isSuccessFlag()) {
                    jobj.put(Constants.RES_success, result.isSuccessFlag());
                    jobj.put("msg", "Transaction approval rules has been saved successfully.");
                }


            } else {
                approvalRulesMap.put("ID", duplicatejObj.getString("ruleid"));
                result = accCostCenterObj.editApprovalRules(approvalRulesMap);
//                    List<ApprovalRules> list = result.getEntityList();
//                    int count = result.getRecordTotalCount();
                jobj.put(Constants.RES_success, result.isSuccessFlag());
                jobj.put("msg", "Transaction approval rules has been saved successfully.");
            }
            int level = Integer.parseInt(request.getParameter("approvallevel"));
            String rule = request.getParameter("rulename");
            String documenttype = request.getParameter("documenttype");
            auditTrailObj.insertAuditLog(AuditAction.APPROVAL_RULE, "User " + sessionHandlerImpl.getUserFullName(request) + " has set approval rule " + rule + " with level " + level + " for " + documenttype, request, id);
            txnManager.commit(status);
        } catch (Exception ex) {
            txnManager.rollback(status);
            Logger.getLogger(AccCostCenterController.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE("saveApprovalRules : " + ex.getMessage(), ex);
        }
        return new ModelAndView("jsonView", "model", jobj.toString());

    }

    public JSONObject getApprovalRulesForDuplicate(String trasactiontype, String fieldtype, String companyid) {

//      boolean duplicate = false;
//      String ruleid = "";
        JSONObject jObj = new JSONObject();
        try {
            HashMap<String, Object> requestParams = new HashMap<String, Object>();//AccountingManager.getGlobalParams(request);

            ArrayList filter_names = new ArrayList(), filter_params = new ArrayList();
            filter_names.add("company.companyID");
            filter_params.add(companyid);
            requestParams.put(Constants.filterNamesKey, filter_names);
            requestParams.put(Constants.filterParamsKey, filter_params);

            jObj.put("duplicate", false);
            jObj.put("ruleid", "");

            KwlReturnObject result = accCostCenterObj.getApprovalRules(requestParams);
            List<ApprovalRules> list = result.getEntityList();
//                for (ApprovalRules ApprovalRules : list) {
//                    if(StringUtil.equal(trasactiontype,ApprovalRules.getTypeid()) && StringUtil.equal(fieldtype,ApprovalRules.getFieldType())){
//                        jObj.put("duplicate", true);
//                        jObj.put("ruleid", ApprovalRules.getID());
//                    }             
//                }

        } catch (Exception ex) {
        }
        return jObj;
    }

    public ModelAndView getApprovalRules(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = null;
        try {
            HashMap<String, Object> requestParams = new HashMap<String, Object>();//AccountingManager.getGlobalParams(request);

            ArrayList filter_names = new ArrayList(), filter_params = new ArrayList();
            String companyid = request.getParameter("companyid");

            if (StringUtil.isNullOrEmpty(companyid)) {
                companyid = sessionHandlerImpl.getCompanyid(request);
            }

            filter_names.add("company.companyID");
            filter_params.add(companyid);
            requestParams.put(Constants.filterNamesKey, filter_names);
            requestParams.put(Constants.filterParamsKey, filter_params);

            KwlReturnObject result = accCostCenterObj.getApprovalRules(requestParams);
            List<ApprovalRules> list = result.getEntityList();
            int count = result.getRecordTotalCount();

            JSONArray jSONArray = new JSONArray();
            for (ApprovalRules ApprovalRules : list) {
                JSONObject jSONObject = new JSONObject();
                jSONObject.put("id", ApprovalRules.getID());
//                jSONObject.put("typeid",ApprovalRules.get);
                jSONObject.put("transactionType", ApprovalRules.getTypeid());
                jSONObject.put("fieldType", ApprovalRules.getFieldType());

                if (StringUtil.equal(ApprovalRules.getFieldType(), "2") || StringUtil.equal(ApprovalRules.getFieldType(), "3")) {
                    String[] valueStr = ApprovalRules.getValue().split(",");
                    String productsName = "";
//                    String sqlQuery = "select productname from product where productid = ?";
                    for (int i = 0; i < valueStr.length; i++) {
                        List productList = accCostCenterObj.getProductName(valueStr[i]);
                        Iterator<Object[]> itr1 = productList.iterator();
                        if (itr1.hasNext()) {
                            Object row = (Object) itr1.next();
                            productsName += row.toString() + ",";
                        }
                    }
                    if (!StringUtil.isNullOrEmpty(productsName)) {
                        productsName = productsName.substring(0, productsName.length() - 1);
                    }
                    jSONObject.put("productname", productsName);
                } else {
                    jSONObject.put("productname", "");
                }
                jSONObject.put("value", ApprovalRules.getValue());
                jSONObject.put("rulename", ApprovalRules.getRuleName());
                jSONObject.put("approvallevel", ApprovalRules.getApprovallevel());
                jSONObject.put("discountamount", ApprovalRules.getDiscountamount());
                jSONArray.put(jSONObject);
            }
            jobj.put(Constants.RES_data, jSONArray);
            jobj.put(Constants.RES_count, count);
            issuccess = true;
        } catch (Exception ex) {
            msg = ex.getMessage();
        } finally {
            try {
                jobj.put(Constants.RES_success, issuccess);
                jobj.put(Constants.RES_msg, msg == null ? "null" : msg);
            } catch (JSONException ex) {
                Logger.getLogger(AccCostCenterController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView(Constants.jsonView, Constants.model, jobj.toString());
    }

    public ModelAndView editApprovalRule(HttpServletRequest request, HttpServletResponse response) {

        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = null;
        KwlReturnObject result = null;
        try {
            HashMap<String, Object> approvalRulesMap = new HashMap<String, Object>();
            approvalRulesMap.put("ID", request.getParameter("ruleid"));
            approvalRulesMap.put("RuleName", request.getParameter("rulename"));
            approvalRulesMap.put("Typeid", request.getParameter("trasactiontype"));
            approvalRulesMap.put("FieldType", request.getParameter("fieldtype"));
            approvalRulesMap.put("Value", request.getParameter("value"));
            if (!StringUtil.isNullOrEmpty(request.getParameter("discountamount"))) {
                approvalRulesMap.put("discount", request.getParameter("discountamount"));
            }
//            approvalRulesMap.put("Company", request.getParameter("companyid"));
            approvalRulesMap.put("approvallevel", Integer.parseInt(request.getParameter("approvallevel")));
            result = accCostCenterObj.editApprovalRules(approvalRulesMap);


            List<ApprovalRules> list = result.getEntityList();
            int count = result.getRecordTotalCount();
            issuccess = result.isSuccessFlag();
            msg = result.getMsg();
            int level = Integer.parseInt(request.getParameter("approvallevel"));
            String rule = request.getParameter("rulename");
            String documenttype = request.getParameter("documenttype");
            auditTrailObj.insertAuditLog(AuditAction.APPROVAL_RULE, "User " + sessionHandlerImpl.getUserFullName(request) + " has updated approval rule " + rule + " with level " + level + " for " + documenttype, request, request.getParameter("ruleid"));
        } catch (Exception ex) {
            msg = ex.getMessage();
        } finally {
            try {
                jobj.put(Constants.RES_success, issuccess);
                jobj.put(Constants.RES_msg, msg == null ? "null" : msg);
            } catch (JSONException ex) {
                Logger.getLogger(AccCostCenterController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView(Constants.jsonView, Constants.model, jobj.toString());
    }

    public ModelAndView deleteApprovalRules(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        KwlReturnObject result = null;
        boolean issuccess = false;
        String msg = "";
        try {
            String companyid = request.getParameter("companyid");

            if (StringUtil.isNullOrEmpty(companyid)) {
                companyid = sessionHandlerImpl.getCompanyid(request);
            }
            String ruleid = request.getParameter("ruleid");
            result = accCostCenterObj.deleteApprovalRules(companyid, ruleid);
            issuccess = result.isSuccessFlag();
            msg = "Transaction approval rule has been deleted successfully.";
            int level = Integer.parseInt(request.getParameter("approvallevel"));
            String rule = request.getParameter("rulename");
            String documenttype = request.getParameter("documenttype");
            auditTrailObj.insertAuditLog(AuditAction.APPROVAL_RULE, "User " + sessionHandlerImpl.getUserFullName(request) + " has deleted approval rule " + rule + " with level " + level + " for " + documenttype, request, companyid);

        } catch (Exception ex) {
            Logger.getLogger(AccCostCenterController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put(Constants.RES_success, issuccess);
                jobj.put(Constants.RES_msg, msg == null ? "null" : msg);
            } catch (JSONException ex) {
                Logger.getLogger(AccCostCenterController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView(Constants.jsonView, Constants.model, jobj.toString());
    }
}
