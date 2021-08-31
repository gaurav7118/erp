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
package com.krawler.spring.accounting.costCenter.service;

import com.krawler.common.admin.CostCenter;
import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.common.util.Constants;
import com.krawler.common.util.StringUtil;
import com.krawler.hql.accounting.AccountingException;
import com.krawler.spring.accounting.costCenter.AccCostCenterDAO;
import com.krawler.spring.accounting.costCenter.CCConstants;
import com.krawler.spring.accounting.handler.AccountingManager;
import com.krawler.spring.auditTrailModule.auditTrailDAO;
import com.krawler.spring.common.KwlReturnObject;
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
import org.springframework.web.servlet.support.RequestContextUtils;
import com.krawler.common.admin.AuditAction;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author krawler
 */
public class AccCostCenterServiceImpl implements AccCostCenterService, MessageSourceAware {

    private MessageSource messageSource;
    private AccCostCenterDAO accCostCenterObj;
    private auditTrailDAO auditTrailObj;

    @Override
    public void setMessageSource(MessageSource ms) {
        this.messageSource = ms;
    }

    public void setaccCostCenterDAO(AccCostCenterDAO accCostCenterDAOObj) {
        this.accCostCenterObj = accCostCenterDAOObj;
    }
    
     public void setauditTrailDAO(auditTrailDAO auditTrailDAOObj) {
        this.auditTrailObj = auditTrailDAOObj;
    }

    public JSONObject getCostCenter(HttpServletRequest request, HttpServletResponse response) {
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

            KwlReturnObject result = accCostCenterObj.getCostCenter(requestParams);
            List<CostCenter> list = result.getEntityList();
            int count = result.getRecordTotalCount();

            JSONArray DataJArr = getCostCenterJson(request, list);
            jobj.put(Constants.RES_data, DataJArr);
            jobj.put(Constants.RES_count, count);
            issuccess = true;
        } catch (Exception ex) {
            msg = ex.getMessage();
        } finally {
            try {
                jobj.put(Constants.RES_success, issuccess);
                jobj.put(Constants.RES_msg, msg == null ? "null" : msg);
            } catch (JSONException ex) {
                Logger.getLogger(AccCostCenterServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return jobj;
    }

    public JSONArray getCostCenterJson(HttpServletRequest request, List<CostCenter> costCenters) throws ServiceException {
        JSONArray jArr = new JSONArray();
        try {
            String forCombo = request.getParameter(CCConstants.REQ_FORCOMBO) == null ? "" : request.getParameter(CCConstants.REQ_FORCOMBO);
            if (!costCenters.isEmpty() && !StringUtil.isNullOrEmpty(forCombo)) {
                JSONObject obj = new JSONObject();
                obj.put(CCConstants.JSON_ID, "");
                obj.put(CCConstants.JSON_CCID, "");
                obj.put(CCConstants.JSON_NAME, forCombo.equalsIgnoreCase(CCConstants.REQ_COMBO_REPORT) ? messageSource.getMessage("acc.rem.110", null, RequestContextUtils.getLocale(request)) : messageSource.getMessage("acc.rem.111", null, RequestContextUtils.getLocale(request)));
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
  
    //Rest Services and request dependency removed
    public JSONArray getCostCenterJson(JSONObject paramJobj, List<CostCenter> costCenters) throws ServiceException {
        JSONArray jArr = new JSONArray();
        try {
            boolean isFormPanel = false;
            if (paramJobj.has("isFormPanel")) {
                isFormPanel = paramJobj.optBoolean("isFormPanel", false);
            }

            if (costCenters.isEmpty() || isFormPanel) {
                JSONObject obj = new JSONObject();
                obj.put(CCConstants.JSON_ID, "");
                obj.put(CCConstants.JSON_CCID, "");
                obj.put(CCConstants.JSON_NAME, "None");
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

 @Override   
 @Transactional(propagation = Propagation.REQUIRED, rollbackFor = {ServiceException.class, JSONException.class, AccountingException.class,SessionExpiredException.class})
    public JSONObject saveCostCenter(JSONObject paramJobj) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
//        boolean isCommitEx = false;

//        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
//        def.setName("CCenter_Tx");
//        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);

//        TransactionStatus status = txnManager.getTransaction(def);
        try {
            JSONObject result = saveCostCenterJSON(paramJobj);
            issuccess = true;
            /*
             * a variable to store the names of cost centers which are being
             * used in other transactions
             */
            String linkedCostCenter = result.optString("isUsedCost");
            /*
             * a variable to store the name of duplicate cost center
             */
            String duplicateCostCenter = result.optString("isDuplicateCost");
            /*
             * if the cost center is not used in other transactions and not
             * duplicate
             */
            if (StringUtil.isNullOrEmpty(linkedCostCenter) && StringUtil.isNullOrEmpty(duplicateCostCenter)) {
                msg = messageSource.getMessage("acc.cc.add", null, Locale.forLanguageTag(paramJobj.optString(Constants.language)));
            } /*
             * if the cost center is used in other transactions and duplicate
             */ else if (!StringUtil.isNullOrEmpty(linkedCostCenter) && !StringUtil.isNullOrEmpty(duplicateCostCenter)) {
                msg = messageSource.getMessage("acc.cc.except", null, Locale.forLanguageTag(paramJobj.optString(Constants.language))) + "  <b>" + linkedCostCenter + " </b>" + " and " + " <b>" + duplicateCostCenter + " </b>.<br><b>" + linkedCostCenter + "</b> " + messageSource.getMessage("acc.cc.used1", null, Locale.forLanguageTag(paramJobj.optString(Constants.language))) + "<br>" + messageSource.getMessage("acc.cc.cantAdd", null, Locale.forLanguageTag(paramJobj.optString(Constants.language))) + "  " + "<b>" + duplicateCostCenter + " " + "</b>" + messageSource.getMessage("acc.cc.exists1", null, Locale.forLanguageTag(paramJobj.optString(Constants.language))) + ".";
            } /*
             * if the cost center is duplicate
             */ else if (!StringUtil.isNullOrEmpty(duplicateCostCenter)) {
                msg = messageSource.getMessage("acc.cc.except", null, Locale.forLanguageTag(paramJobj.optString(Constants.language))) + " " + "<b>" + duplicateCostCenter + "</b>.<br>" + messageSource.getMessage("acc.cc.exists2", null, Locale.forLanguageTag(paramJobj.optString(Constants.language))) + ".";
            } /*
             * if the cost center is used in other transactions
             */ else if (!StringUtil.isNullOrEmpty(linkedCostCenter)) {
                msg = messageSource.getMessage("acc.cc.except", null, Locale.forLanguageTag(paramJobj.optString(Constants.language))) + " " + "<b>" + " " + linkedCostCenter + "</b>.<br>" + messageSource.getMessage("acc.cc.used2", null, Locale.forLanguageTag(paramJobj.optString(Constants.language)));
            }

            try {
//                txnManager.commit(status);
            } catch (Exception ex) {
//                isCommitEx = true;
                msg = ex.getMessage();
            }
        } catch (Exception ex) {
//            if (!isCommitEx) {
//                txnManager.rollback(status);
//            }
            msg = "" + ex.getMessage();
            Logger.getLogger(AccCostCenterServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put(Constants.RES_success, issuccess);
                jobj.put(Constants.RES_msg, msg);
            } catch (JSONException ex) {
                Logger.getLogger(AccCostCenterServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return jobj;
    }
    
    public JSONObject saveCostCenterJSON(JSONObject paramJobj) throws ServiceException, AccountingException, SessionExpiredException, JSONException {
        /*
         * an object to store the names of cost centers
         */
        JSONObject jObj = new JSONObject();
        KwlReturnObject result = null;
        try {
            JSONArray jArr = new JSONArray(paramJobj.optString(Constants.data, "[{}]"));
            JSONArray jDelArr = new JSONArray(paramJobj.optString("deleteddata", "[{}]"));
            String companyid = paramJobj.optString(Constants.companyKey);
            String id = "", ccid = "", ccname = "";

            //Audit Trail configs
            Map<String, Object> auditRequestParams = new HashMap<String, Object>();
            auditRequestParams.put(Constants.reqHeader, paramJobj.getString(Constants.reqHeader));
            auditRequestParams.put(Constants.remoteIPAddress, paramJobj.getString(Constants.remoteIPAddress));
            auditRequestParams.put(Constants.useridKey, paramJobj.getString(Constants.useridKey));

            /*
             * variables to store the names of used and duplicate cost center
             */
            String linkedCostCenter = "", duplicateCostCenter = "";

            for (int i = 0; i < jDelArr.length(); i++) {
                String deleteid = "";
                JSONObject jobj = jDelArr.getJSONObject(i);
                if (!StringUtil.isNullOrEmpty(jobj.getString("id"))) {
                    try {
                        deleteid = jobj.getString("id");
                        String deletename = jobj.getString("name");
                        result = accCostCenterObj.deleteCostCenter(deleteid, companyid);


                        if (result.isSuccessFlag()) {
                            /*
                             * making an entry of deleted cost center in
                             * auditTrailObj
                             */
//                            auditTrailObj.insertAuditLog(AuditAction.COST_CENTER_DELETED, "User " + sessionHandlerImpl.getUserFullName(request) + " has deleted Cost Center " + deletename, request, id);
                            auditTrailObj.insertAuditLog(AuditAction.COST_CENTER_DELETED, "User " + paramJobj.optString(Constants.userfullname) + " has deleted Cost Center " + deletename, auditRequestParams, id);
                        } else {
                            /*
                             * check to append comma in between names of linked
                             * cost centers
                             */
                            if (i > 0 && i != jDelArr.length()) {
                                linkedCostCenter += " , ";
                            }
                            /*
                             * storing the name of cost center which is being
                             * used in other transactions
                             */
                            linkedCostCenter += deletename;
                        }
                    } catch (ServiceException ex) {
                        throw new AccountingException(messageSource.getMessage("acc.cc.excp1", null, Locale.forLanguageTag(paramJobj.optString(Constants.language))));   //"The Cost Center(s) is or had been used in transaction(s). So, it cannot be deleted.");
                    }
                }
            }
            HashMap<String, Object> ccMap;
            int cntDuplicate = 0;
            KwlReturnObject unqRes;
            for (int i = 0; i < jArr.length(); i++) {
                JSONObject jobj = jArr.getJSONObject(i);
                if (jobj.getBoolean("modified") == false) {
                    continue;
                }
                ccid = StringUtil.DecodeText(jobj.optString(CCConstants.JSON_CCID));
                ccname = StringUtil.DecodeText(jobj.optString(CCConstants.JSON_NAME));
                id = jobj.getString(CCConstants.JSON_ID);
                unqRes = accCostCenterObj.checkUniqueCostCenter(id, ccid, ccname, companyid);
                if (unqRes.getRecordTotalCount() > 0) {
                    /*
                     * check to append comma in between names of duplicate cost
                     * centers
                     */
                    if (cntDuplicate > 0) {
                        duplicateCostCenter += " , ";
                    }
                    /*
                     * storing the name of cost center which is being added
                     * duplicately
                     */
                    duplicateCostCenter += ccname;
                    cntDuplicate += 1;
                } else {
                    ccMap = new HashMap<String, Object>();
                    ccMap.put("Ccid", ccid);
                    ccMap.put("Name", ccname);
                    ccMap.put("Description", StringUtil.DecodeText(jobj.optString(CCConstants.JSON_DESC)));
                    ccMap.put("Company", companyid);

                    if (!StringUtil.isNullOrEmpty(id)) {
                        ccMap.put("ID", id);
                    }
                    accCostCenterObj.saveCostCenter(ccMap);
                    String action = "updated";
                    if (StringUtil.isNullOrEmpty(jobj.getString("id"))) {
                        action = "added";
                    }
//                    auditTrailObj.insertAuditLog(AuditAction.COST_CENTER_ADDED, "User " + sessionHandlerImpl.getUserFullName(request) + " has " + action + " Cost Center " + ccname, request, id);
                    auditTrailObj.insertAuditLog(AuditAction.COST_CENTER_ADDED, "User " + paramJobj.optString(Constants.userfullname) + " has " + action + " Cost Center " + ccname, auditRequestParams, id);
                }
            }
            /*
             * binding the success flag,cost center names in the JSONObject
             */
            jObj.put(Constants.RES_success, true);
            jObj.put("isUsedCost", linkedCostCenter);
            jObj.put("isDuplicateCost", duplicateCostCenter);
        } /*
         * catch(UnsupportedEncodingException ex){
         * jObj.put(Constants.RES_success, false); throw
         * ServiceException.FAILURE(messageSource.getMessage("acc.common.excp",
         * null, RequestContextUtils.getLocale(request)), ex); //"Can't extract
         * the records. <br>Encoding not supported", ex);
        }
         */ catch (JSONException ex) {
            jObj.put(Constants.RES_success, false);
            throw ServiceException.FAILURE(ex.getMessage(), ex);   //"Can't extract the records. <br>Encoding not supported", ex);
        } catch (AccountingException ex) {
            jObj.put(Constants.RES_success, false);
            throw ServiceException.FAILURE(ex.getMessage(), ex);   //"Can't extract the records. <br>Encoding not supported", ex);
        }
        return jObj;
    }
    
}