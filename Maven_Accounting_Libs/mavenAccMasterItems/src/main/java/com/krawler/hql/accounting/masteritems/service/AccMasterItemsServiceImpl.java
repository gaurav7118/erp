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
package com.krawler.hql.accounting.masteritems.service;

import com.krawler.common.admin.*;
import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.common.util.Constants;
import com.krawler.common.util.Paging;
import com.krawler.common.util.StringUtil;
import com.krawler.common.util.URLUtil;
import com.krawler.esp.handlers.APICallHandlerService;
import com.krawler.esp.handlers.StorageHandler;
import com.krawler.esp.utils.ConfigReader;
import com.krawler.hql.accounting.AccountingException;
import com.krawler.hql.accounting.MasterGroup;
import com.krawler.hql.accounting.MasterItem;
import com.krawler.inventory.model.inspection.InspectionArea;
import com.krawler.inventory.model.inspection.InspectionTemplate;
import com.krawler.inventory.model.inspection.TemplateService;
import com.krawler.inventory.model.store.Store;
import com.krawler.inventory.model.store.StoreType;
import com.krawler.spring.accounting.costCenter.CCConstants;
import com.krawler.spring.accounting.handler.AccountingHandlerDAO;
import com.krawler.spring.accounting.handler.AccountingManager;
import com.krawler.spring.accounting.masteritems.accMasterItemsController;
import com.krawler.spring.accounting.masteritems.accMasterItemsDAO;
import com.krawler.spring.auditTrailModule.auditTrailDAO;
import com.krawler.spring.authHandler.authHandler;
import com.krawler.spring.common.AccCommonTablesDAO;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.spring.common.fieldManagerDAO;
import com.krawler.spring.common.kwlCommonTablesDAO;
import com.krawler.spring.sessionHandler.sessionHandlerImpl;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.io.IOException;
import java.text.DateFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import org.springframework.context.MessageSource;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.support.RequestContextUtils;
import com.krawler.spring.common.fieldDataManager;
import com.krawler.spring.importFunctionality.ImportDAO;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

/**
 *
 * @author krawler
 */
public class AccMasterItemsServiceImpl implements AccMasterItemsService {

    private accMasterItemsDAO accMasterItemsDAOobj;
    private APICallHandlerService apiCallHandlerService; 
    private AccountingHandlerDAO accountingHandlerDAOobj;
    private kwlCommonTablesDAO kwlCommonTablesDAOObj;
    private AccCommonTablesDAO accCommonTablesDAO;
    private fieldManagerDAO fieldManagerDAOobj;
    private auditTrailDAO auditTrailObj;
    private TemplateService templateService;
    private MessageSource messageSource;
    private fieldDataManager fieldDataManagercntrl;
    private ImportDAO importDao;
    
    public TemplateService getTemplateService() {
        return templateService;
    }

    public void setTemplateService(TemplateService templateService) {
        this.templateService = templateService;
    }

    public fieldDataManager getFieldDataManagercntrl() {
        return fieldDataManagercntrl;
    }

    public void setFieldDataManagercntrl(fieldDataManager fieldDataManagercntrl) {
        this.fieldDataManagercntrl = fieldDataManagercntrl;
    }
    
    public void setMessageSource(MessageSource ms) {
        this.messageSource = ms;
    }

    public void setaccMasterItemsDAO(accMasterItemsDAO accMasterItemsDAOobj) {
        this.accMasterItemsDAOobj = accMasterItemsDAOobj;
    }
    public void setApiCallHandlerService(APICallHandlerService apiCallHandlerService) {     
        this.apiCallHandlerService = apiCallHandlerService;
    }
    
    public void setaccountingHandlerDAO(AccountingHandlerDAO AccountingHandlerDAOobj) {
        this.accountingHandlerDAOobj = AccountingHandlerDAOobj;
    }
    
    public void setkwlCommonTablesDAO(kwlCommonTablesDAO kwlCommonTablesDAOObj) {
        this.kwlCommonTablesDAOObj = kwlCommonTablesDAOObj;
    }
    public void setaccCommonTablesDAO(AccCommonTablesDAO accCommonTablesDAO) {
        this.accCommonTablesDAO = accCommonTablesDAO;
    }
    public void setFieldManagerDAO(fieldManagerDAO fieldManagerDAOobj) {
        this.fieldManagerDAOobj = fieldManagerDAOobj;
    }
    public void setauditTrailDAO(auditTrailDAO auditTrailDAOObj) {
        this.auditTrailObj = auditTrailDAOObj;
    }
    public void setimportDAO(ImportDAO importDao) {
        this.importDao = importDao;
    }
    @Override//getting masteritems
    public JSONObject getMasterItems(HttpServletRequest request) throws ServiceException, SessionExpiredException {
        JSONObject jobj = new JSONObject();
        try {
            JSONArray jArr = new JSONArray();
            HashMap<String, Object> filterRequestParams = new HashMap<String, Object>();
            ArrayList filter_names = new ArrayList(), filter_params = new ArrayList(), order_by = new ArrayList(), order_type = new ArrayList();
            String grID = request.getParameter("groupid");
            String forCombo = request.getParameter(CCConstants.REQ_FORCOMBO) == null ? "" : request.getParameter(CCConstants.REQ_FORCOMBO);
            if (grID.equals("19")) { // if Product Category send data with parent child info
                filter_names.add("masterGroup.ID");
                filter_params.add(grID);
                filter_names.add("company.companyID");
                filter_params.add(sessionHandlerImpl.getCompanyid(request));
                order_by.add("value");
                order_type.add("asc");
                filterRequestParams.put("filter_names", filter_names);
                filterRequestParams.put("filter_params", filter_params);
                filterRequestParams.put("order_by", order_by);
                filterRequestParams.put("order_type", order_type);
                KwlReturnObject result = accMasterItemsDAOobj.getMasterItemsHire(filterRequestParams);

                List list = result.getEntityList();
                if (!list.isEmpty() && !StringUtil.isNullOrEmpty(forCombo)) {
                    JSONObject obj = new JSONObject();
                    obj.put("id", "");
                    obj.put("name", "None");
                    obj.put("parentid", "");
                    obj.put("leaf", "");
                    obj.put("level", "");
                    jArr.put(obj);
                }
                Iterator itr = list.iterator();
                while (itr.hasNext()) {
                    Object[] row = (Object[]) itr.next();
                    MasterItem fieldComboData = (MasterItem) row[0];
                    JSONObject obj = new JSONObject();
                    try {
                        obj.put("id", fieldComboData.getID());
                    } catch (JSONException ex) {
                        Logger.getLogger(AccMasterItemsServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    obj.put("name", fieldComboData.getValue());
                    MasterItem parentItem = (MasterItem) row[3];
                    if (parentItem != null) {
                        obj.put("parentid", parentItem.getID());
                        obj.put("parentname", parentItem.getValue());
                    }
                    obj.put("level", row[1]);
                    obj.put("leaf", row[2]);
                    jArr.put(obj);
                }
            } else {
                filter_names.add("masterGroup.ID");
                filter_params.add(grID);
                filter_names.add("company.companyID");
                filter_params.add(sessionHandlerImpl.getCompanyid(request));
                order_by.add("value");
                order_type.add("asc");
                filterRequestParams.put("filter_names", filter_names);
                filterRequestParams.put("filter_params", filter_params);
                filterRequestParams.put("order_by", order_by);
                filterRequestParams.put("order_type", order_type);
                KwlReturnObject result = accMasterItemsDAOobj.getMasterItems(filterRequestParams);

                List list = result.getEntityList();
                if (!list.isEmpty() && !StringUtil.isNullOrEmpty(forCombo)) {
                    JSONObject obj = new JSONObject();
                    obj.put("id", "");
                    obj.put("name", "None");
                    obj.put("parentid", "");
                    obj.put("leaf", "");
                    obj.put("level", 0);
                    jArr.put(obj);
                }
                if (list != null && !list.isEmpty()) {
                    Iterator itr = list.iterator();
                    while (itr.hasNext()) {
                        MasterItem item = (MasterItem) itr.next();
                        JSONObject obj = new JSONObject();
                        obj.put("id", item.getID());
                        obj.put("name", item.getValue());
                        obj.put("parentid", "");
                        obj.put("leaf", true);
                        obj.put("level", 0);
                        jArr.put(obj);
                    }
                }
            }
            jobj.put("data", jArr);
        } catch (JSONException e) {
            throw ServiceException.FAILURE(e.getMessage(), e);
        }
        return jobj;
    }
    
    @Override//JSONArray for Rest Services
    public JSONArray getMasterItems(JSONObject jobj) throws ServiceException {
        JSONArray jArr = new JSONArray();
        try {
            HashMap<String, Object> filterRequestParams = new HashMap<String, Object>();
            ArrayList filter_names = new ArrayList(), filter_params = new ArrayList(), order_by = new ArrayList(), order_type = new ArrayList();
            String companyid = (String) jobj.get(Constants.companyKey);
            String grID = jobj.has("groupid") ? (String) jobj.get("groupid") : "";
            if (grID.equals(MasterGroup.ProductCategory)) { // if Product Category send data with parent child info
                filter_names.add("masterGroup.ID");
                filter_params.add(grID);
                filter_names.add("company.companyID");
                filter_params.add(companyid);
                order_by.add("value");
                order_type.add("asc");
                filterRequestParams.put("filter_names", filter_names);
                filterRequestParams.put("filter_params", filter_params);
                filterRequestParams.put("order_by", order_by);
                filterRequestParams.put("order_type", order_type);
                KwlReturnObject result = accMasterItemsDAOobj.getMasterItemsHire(filterRequestParams);
               List<Object[]> list = result.getEntityList();
                if (!list.isEmpty()) {
                    JSONObject obj = new JSONObject();
                    obj.put(Constants.Acc_id, "");
                    obj.put(Constants.Acc_name, "None");
                    obj.put(Constants.Acc_parentid, "");
                    obj.put(Constants.Acc_leaf, "");
                    obj.put(Constants.Acc_level, "");
                    jArr.put(obj);
                }
                Iterator itr = list.iterator();
                for (int i = 0; i < list.size(); i++) {
                    Object[] row = list.get(i);
                    MasterItem fieldComboData = (MasterItem) row[0];
                    JSONObject obj = new JSONObject();
                    try {
                        obj.put(Constants.Acc_id, fieldComboData.getID());
                    } catch (JSONException ex) {
                        Logger.getLogger(AccMasterItemsServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    obj.put(Constants.Acc_name, fieldComboData.getValue());
                    MasterItem parentItem = (MasterItem) row[3];
                    if (parentItem != null) {
                        obj.put(Constants.Acc_parentid, parentItem.getID());
                        obj.put(Constants.Acc_parentname, parentItem.getValue());
                    }
                    obj.put(Constants.Acc_level, row[1]);
                    obj.put(Constants.Acc_leaf, row[2]);
                    jArr.put(obj);
                }
            } else {//for salesperson
                if (jobj.has(Constants.ss) && !StringUtil.isNullOrEmpty((String) jobj.get(Constants.ss))) {
                    String ss = (String) jobj.get(Constants.ss);
                    filterRequestParams.put(Constants.ss, ss);
                    filterRequestParams.put("ss_names", new String[]{"value"});
                }

                filter_names.add("masterGroup.ID");
                filter_params.add(grID);
                filter_names.add("company.companyID");
                filter_params.add(companyid);
                order_by.add("value");
                order_type.add("asc");
                filterRequestParams.put("filter_names", filter_names);
                filterRequestParams.put("filter_params", filter_params);
                filterRequestParams.put("order_by", order_by);
                filterRequestParams.put("order_type", order_type);
                KwlReturnObject result = accMasterItemsDAOobj.getMasterItems(filterRequestParams);

                List list = result.getEntityList();
                if (!list.isEmpty()) {
                    JSONObject obj = new JSONObject();
                    obj.put(Constants.Acc_id, "");
                    obj.put(Constants.Acc_name, "None");
                    obj.put(Constants.Acc_parentid, "");
                    obj.put(Constants.Acc_leaf, "");
                    obj.put(Constants.Acc_level, "");
                    jArr.put(obj);
                }
                for (int i = 0; i < list.size(); i++) {

                    MasterItem item = (MasterItem) list.get(i);
                    JSONObject obj = new JSONObject();
                    obj.put(Constants.Acc_id, item.getID());
                    obj.put(Constants.Acc_name, item.getValue());
                    obj.put(Constants.Acc_parentid, "");
                    obj.put(Constants.Acc_leaf, true);
                    obj.put(Constants.Acc_level, 0);
                    obj.put("salespersoncode", item.getCode());
                    obj.put("salesPersonContactNumber", item.getContactNumber());
                    obj.put("salesPersonAddress", item.getAddress());
                    obj.put("salesPersonDesignation", item.getDesignation());
                    obj.put("activated", item.isActivated());
                    obj.put("hasAccess", item.isActivated());
                    obj.put("isIbgActivItematedForPaidTo", item.isIbgActivated());
                    obj.put("groupid", grID);//For Identifing group Value
                    obj.put("emailid", (item.getEmailID() != null) ? item.getEmailID() : "");
                    obj.put("userid", (item.getUser() != null) ? item.getUser().getUserID() : "");
                    obj.put("typeid", item.getCustVendCategoryType());
                    obj.put("driverID", (item.getDriver() != null) ? item.getDriver().getID() : "");
                    obj.put("isDefaultToPOS", item.isDefaultToPOS());
                    obj.put("defaultMasterItem", (item.getDefaultMasterItem() != null) ? item.getDefaultMasterItem().getID() : "");
                    obj.put("natureofpaymentdesc", item.getValue());
                    obj.put("natureofpaymentsection", item.getCode());
                    obj.put("typeofdeducteetype", item.getCode());
                    obj.put("vatcommoditycode", item.getVatcommoditycode());
                    obj.put("vatscheduleno", item.getVatscheduleno());
                    obj.put("vatscheduleserialno", item.getVatscheduleserialno());
                    obj.put("vatnotes", item.getVatnotes());

                    jArr.put(obj);
                }
            }
        } catch (JSONException e) {
            throw ServiceException.FAILURE(e.getMessage(), e);
        }
        return jArr;
    }
    
 @Override//getting masteritems
    public JSONObject getMasterItemsForEclaim(HttpServletRequest request) throws ServiceException, SessionExpiredException {
        Map<String, Object> requestParams = new HashMap();
        String companyid = request.getParameter("companyid");
        if (!StringUtil.isNullOrEmpty(companyid)) {
            requestParams.put("companyid", companyid);
        }
        return getMasterItemsForEclaim(requestParams);
    }
    
    @Override//getting masteritems
    public JSONObject getMasterItemsForEclaim(Map<String, Object> requestParams) throws ServiceException, SessionExpiredException {
        JSONObject jobj = new JSONObject();
        try {
            JSONArray jArr = new JSONArray();
            HashMap<String, Object> filterRequestParams = new HashMap<String, Object>();
            ArrayList filter_names = new ArrayList(), filter_params = new ArrayList(), order_by = new ArrayList(), order_type = new ArrayList();
            if (requestParams.containsKey("companyid") && requestParams.get("companyid") != null) {
                filter_names.add("company.companyID");
                filter_params.add(requestParams.get("companyid").toString());
            }
            
            filter_names.add("isforeclaim");
            filter_params.add(1);
            filterRequestParams.put("filter_names", filter_names);
            filterRequestParams.put("filter_values", filter_params);
            KwlReturnObject result = accMasterItemsDAOobj.getFieldParams(filterRequestParams);
            List list = result.getEntityList();
            String masterID = null;
            if (!list.isEmpty()) {
                masterID = ((FieldParams) list.get(0)).getId();
            }
            
            filter_names.clear();
            filter_params.clear();
            filterRequestParams.clear();
            
            filter_names.add("field.id");
            filter_params.add(masterID);
            order_by.add("value");
            order_type.add("asc");
            filterRequestParams.put("filter_names", filter_names);
            filterRequestParams.put("filter_params", filter_params);
            filterRequestParams.put("order_by", order_by);
            filterRequestParams.put("order_type", order_type);
            result = accMasterItemsDAOobj.getMasterItemsForCustom(filterRequestParams);
            Iterator itr = result.getEntityList().iterator();
            
            while (itr.hasNext()) {
                FieldComboData fieldComboData = (FieldComboData) itr.next();
                JSONObject obj = new JSONObject();
                obj.put(CCConstants.JSON_ID, fieldComboData.getId());
                obj.put(CCConstants.JSON_NAME, fieldComboData.getValue());
                obj.put(CCConstants.JSON_DESC, StringUtil.isNullOrEmpty(fieldComboData.getItemdescription())?"":fieldComboData.getItemdescription());
                jArr.put(obj);
            }
            jobj.put("data", jArr);
            jobj.put("totalCount", jArr.length());
        } catch (JSONException e) {
            throw ServiceException.FAILURE(e.getMessage(), e);
        }
        return jobj;
    }
    
    @Override
    public JSONObject addEditMasterItemsForEclaim(HttpServletRequest request) throws ServiceException, SessionExpiredException {
        JSONObject jobj = new JSONObject();
        try {
            JSONArray responseArr = new JSONArray();
            HashMap<String, Object> filterRequestParams = new HashMap<String, Object>();
            ArrayList filter_names = new ArrayList(), filter_params = new ArrayList(), order_by = new ArrayList(), order_type = new ArrayList();
            
            filter_names.add("isforeclaim");
            filter_params.add(1);
            filterRequestParams.put("filter_names", filter_names);
            filterRequestParams.put("filter_values", filter_params);
            KwlReturnObject result = accMasterItemsDAOobj.getFieldParams(filterRequestParams);
            List list = result.getEntityList();
            String masterID = null;
            if (!list.isEmpty()) {
                masterID = ((FieldParams) list.get(0)).getId();
            }
            
            HashMap requestParam = AccountingManager.getGlobalParams(request);
            String costcenterids = request.getParameter("costcenterids") != null ? request.getParameter("costcenterids") : "[]";
            JSONArray jArr = new JSONArray(costcenterids);
            for (int i = 0; i < jArr.length(); i++) {
                JSONObject obj = jArr.getJSONObject(i);
                requestParam.put("id", StringUtil.isNullOrEmpty(obj.getString("erpid")) ? "" : obj.getString("erpid"));
                requestParam.put("name", obj.optString("name"));
                requestParam.put("itemdescription", obj.optString("itemdescription"));
                requestParam.put("eclaimid", obj.optString("eclaimid"));
                requestParam.put("groupid", masterID);

                result = accMasterItemsDAOobj.addUpdateMasterCustomItem(requestParam, false);
                
                list = result.getEntityList();
                if (!list.isEmpty()) {
                    JSONObject jObj = new JSONObject();
                    String comboID = ((FieldComboData) list.get(0)).getId();
                    jObj.put("eclaimid", obj.optString("eclaimid"));
                    jObj.put("erpid", comboID);
                    responseArr.put(jObj);
                }
            }
            jobj.put("data", jArr);
        } catch (JSONException e) {
            throw ServiceException.FAILURE(e.getMessage(), e);
        }
        return jobj;
    }
    
    @Override
    public JSONObject deleteMasterItemsForEclaim(HttpServletRequest request) throws ServiceException, SessionExpiredException {
        JSONObject jobj = new JSONObject();
        try {
            HashMap<String, Object> filterRequestParams = new HashMap<String, Object>();
            ArrayList filter_names = new ArrayList(), filter_params = new ArrayList(), order_by = new ArrayList(), order_type = new ArrayList();
            
            filter_names.add("isforeclaim");
            filter_params.add(1);
            filter_names.add("company.companyID");
            filter_params.add(sessionHandlerImpl.getCompanyid(request));
            filterRequestParams.put("filter_names", filter_names);
            filterRequestParams.put("filter_values", filter_params);
            KwlReturnObject result = accMasterItemsDAOobj.getFieldParams(filterRequestParams);
            List list = result.getEntityList();
            String masterID = null;
            if (!list.isEmpty()) {
                masterID = ((FieldParams) list.get(0)).getId();
            }
            
            String usedIds = "";
            boolean isused = false;
            JSONArray jArr = new JSONArray();
            JSONObject jobdata = new JSONObject();
//            for (int i = 0; i < jArr.length(); i++) {
                JSONObject obj = new JSONObject(request.getParameter("data"));
                String appuiid = StringUtil.isNullOrEmpty(obj.getString("appuiid")) ? "" : obj.getString("appuiid");
            
                //Check whether the record is exist or not.
                KwlReturnObject comboresult = accountingHandlerDAOobj.getObject(FieldComboData.class.getName(),appuiid);
                FieldComboData fieldComboData = (FieldComboData) comboresult.getEntityList().get(0);
                if(fieldComboData!=null){
                    //Check whether the record is used or not
                    boolean isUsed = accMasterItemsDAOobj.isUsedMasterCustomItem(appuiid, masterID);
                    if (!isUsed) {
                        KwlReturnObject delresult = accMasterItemsDAOobj.daleteMasterCustomItem(appuiid);
                        jobdata.put("isused", false);
                        jobdata.put("isdeleted", delresult.isSuccessFlag());
                        jobdata.put("msg", delresult.getMsg());
                    } else {
                        jobdata.put("isused", true);
                        jobdata.put("isdeleted", false);
                        jobdata.put("msg", "Cost Center cannot be delete. It is used in transaction.");
                    }
                } else {    //Record doesn't exist
                    jobdata.put("isused", false);
                    jobdata.put("isdeleted", false);
                    jobdata.put("msg", "Cost Center is not exist.");
                }

//                boolean isUsed = accMasterItemsDAOobj.isUsedMasterCustomItem(id, masterID);
//                if (!isUsed) {
//                    accMasterItemsDAOobj.daleteMasterCustomItem(id);
//                } else {
//                    usedIds += id + ", ";
//                }
            //}
            jArr.put(jobdata);
            jobj.put("data", jArr);
        } catch (JSONException e) {
            throw ServiceException.FAILURE(e.getMessage(), e);
        }
        return jobj;
    }
    public JSONObject sendProcessSkill(Map<String, Object> requestParams) throws ServiceException, SessionExpiredException {
        JSONObject resObj = new JSONObject();
        try {
            String companyId = "";
            if (requestParams.containsKey("companyId")) {
                companyId = (String) requestParams.get("companyId");
            }
            JSONArray dataJArr = new JSONArray();
            HashMap<String, Object> filterRequestParams = new HashMap<String, Object>();
            ArrayList filter_names = new ArrayList(), filter_params = new ArrayList(), order_by = new ArrayList(), order_type = new ArrayList();
            filter_names.add("masterGroup.ID");
            filter_params.add("36");
            filter_names.add("company.companyID");
            filter_params.add(companyId);
            order_by.add("value");
            order_type.add("asc");
            filterRequestParams.put("filter_names", filter_names);
            filterRequestParams.put("filter_params", filter_params);
            filterRequestParams.put("order_by", order_by);
            filterRequestParams.put("order_type", order_type);
            KwlReturnObject result = accMasterItemsDAOobj.getMasterItems(filterRequestParams);
            List<MasterItem> list = result.getEntityList();
            int count = result.getRecordTotalCount();
            for (MasterItem masterItem : list) {
                JSONObject jSONObject = new JSONObject();
                jSONObject.put("id", masterItem.getID());
                jSONObject.put("value", masterItem.getValue());
                jSONObject.put("groupname", masterItem.getMasterGroup().getGroupName());
                jSONObject.put("groupid", masterItem.getMasterGroup().getID());
                jSONObject.put("companyid", companyId);
                dataJArr.put(jSONObject);
            }
            filter_params = new ArrayList();
            filter_params.add("54");
            filter_params.add(companyId);
            filterRequestParams.put("filter_params", filter_params);
            result = accMasterItemsDAOobj.getMasterItems(filterRequestParams);
            list = result.getEntityList();
            count = result.getRecordTotalCount();
            for (MasterItem masterItem : list) {
                JSONObject jSONObject = new JSONObject();
                jSONObject.put("id", masterItem.getID());
                jSONObject.put("value", masterItem.getValue());
                jSONObject.put("groupname", masterItem.getMasterGroup().getGroupName());
                jSONObject.put("companyid", companyId);
                dataJArr.put(jSONObject);
            }
            String accRestURL = URLUtil.buildRestURL("pmURL");   
            JSONObject userData = new JSONObject();
            String userId = "";
            if (requestParams.containsKey("userId")) {
                userId = (String) requestParams.get("userId");
            }
            userData.put("iscommit", true);
            userData.put("userid", userId);
            userData.put("companyid", companyId);
            userData.put("inputdata", dataJArr);
            String endpoint = accRestURL + "transaction/process";
            resObj = apiCallHandlerService.restPostMethod(endpoint, userData.toString());
        } catch (JSONException ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
        return resObj;
    }
    
    /**
     * 
     * @param map = Include params and Master Item sync to PM
     * @return 
     */
    public JSONObject sendMasterToPM(Map map) {
        JSONObject resObj = new JSONObject();
        try {
            String accRestURL = URLUtil.buildRestURL("pmURL");
            JSONObject userData = new JSONObject();
            String userId = "";
            String companyId = "";
            MasterItem masterItem = null;
            if (map.containsKey("userId")) {
                userId = (String) map.get("userId");
            }
            if (map.containsKey("companyId")) {
                companyId = (String) map.get("companyId");
            }
            if (map.containsKey("masterItem")) {
                masterItem = (MasterItem) map.get("masterItem");
            }
            JSONArray dataJArr = new JSONArray();
            if (masterItem != null) {
                JSONObject jSONObject = new JSONObject();
                jSONObject.put("id", masterItem.getID());
                jSONObject.put("value", masterItem.getValue());
                jSONObject.put("groupname", masterItem.getMasterGroup().getGroupName());
                jSONObject.put("groupid",masterItem.getMasterGroup().getID());
                jSONObject.put("companyid", companyId);
                dataJArr.put(jSONObject);
            }

            userData.put("iscommit", true);
            userData.put("userid", userId);
            userData.put("companyid", companyId);
//            JSONObject inputData = new JSONObject();
            userData.put("inputdata", dataJArr);
            String endpoint = accRestURL + "transaction/process";
            resObj = apiCallHandlerService.restPostMethod(endpoint, userData.toString());

        } catch (JSONException ex) {
            Logger.getLogger(AccMasterItemsServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ServiceException ex) {
            Logger.getLogger(AccMasterItemsServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return resObj;
    }

    @Override
    public JSONObject getInspectionAreaList(Map<String, Object> requestParams) throws ServiceException, SessionExpiredException {
        JSONObject jobj = new JSONObject();
        JSONArray jArr = new JSONArray();
        try {
            String companyId = "";
            String searchString = "";
            Paging paging = null;
            String templateId = "";
            if (!StringUtil.isNullOrEmpty((String) requestParams.get(Constants.companyKey))) {
                companyId = (String) requestParams.get(Constants.companyKey);
                templateId = (String) requestParams.get("templateId");
                InspectionTemplate inspectionTemplate = templateService.getInspectionTemplate(templateId);
                if (inspectionTemplate != null) {
                    List<InspectionArea> iAreaList = templateService.getInspectionAreaList(inspectionTemplate, searchString, paging);
                    for (InspectionArea ia : iAreaList) {
                        JSONObject jObject = new JSONObject();
                        jObject.put("areaId", ia.getId());
                        jObject.put("areaName", ia.getName());
                        jObject.put("faults", ia.getFaults());
                        jObject.put("passingValue", ia.getPassingValue());
                        jObject.put("templateId", templateId);
                        jArr.put(jObject);
                    }
                }
            }
            jobj.put("data", jArr);
        } catch (JSONException e) {
            throw ServiceException.FAILURE(e.getMessage(), e);
        }
        return jobj;
    }

    @Override
    public JSONObject getInspectionTemplateList(Map<String, Object> requestParams) throws ServiceException, SessionExpiredException {
        JSONObject jobj = new JSONObject();
        JSONArray jArr = new JSONArray();
        try {
            String companyId = "";
            String searchString = "";
            Paging paging = null;
            if (!StringUtil.isNullOrEmpty((String) requestParams.get(Constants.companyKey))) {
                companyId=(String) requestParams.get(Constants.companyKey);
                KwlReturnObject jeresult = accountingHandlerDAOobj.getObject(Company.class.getName(), companyId);
                Company company = (Company) jeresult.getEntityList().get(0);
                List<InspectionTemplate> iTemplateList = templateService.getInspectionTemplateList(company, searchString, paging);
                for (InspectionTemplate it : iTemplateList) {
                    JSONObject jObject = new JSONObject();
                    jObject.put("templateId", it.getId());
                    jObject.put("templateName", it.getName());
                    jObject.put("templateDescription", it.getDescription());
                    jArr.put(jObject);
                }
            }
            jobj.put("data", jArr);
        } catch (JSONException e) {
            throw ServiceException.FAILURE(e.getMessage(), e);
        }
        return jobj;
    }
    
    @Override
    public JSONObject getWarehouseItems(Map<String, Object> requestParams) throws ServiceException, SessionExpiredException {
        JSONObject jobj = new JSONObject();
        try {
            boolean isForCustomer = requestParams.containsKey("isForCustomer") ? (Boolean) requestParams.get("isForCustomer") : false;
            HashMap<String, Object> filterRequestParams = new HashMap<>();
            ArrayList filter_names = new ArrayList(), filter_params = new ArrayList(), order_by = new ArrayList(), order_type = new ArrayList();
            filter_names.add("s.company.companyID");
            filter_params.add(requestParams.get("companyid"));
            if (!StringUtil.isNullOrEmpty((String) requestParams.get("customerid"))) {
                filter_names.add("customer");
                filter_params.add((String) requestParams.get("customerid"));
            }
            if (!StringUtil.isNullOrEmpty((String) requestParams.get("movementtypeid"))) {
                filter_names.add("m.id");
                filter_params.add((String) requestParams.get("movementtypeid"));
            }
            filter_names.add("isForCustomer");
            filter_params.add(isForCustomer);
            filter_names.add("s.active");
            filter_params.add(true);
            KwlReturnObject extracompanyprefObjresult = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(),(String) requestParams.get("companyid"));
            ExtraCompanyPreferences extracompanyobj = (ExtraCompanyPreferences) extracompanyprefObjresult.getEntityList().get(0);            
            order_by.add("name");
            order_type.add("asc");
            filterRequestParams.put("filter_names", filter_names);
            filterRequestParams.put("filter_params", filter_params);
            filterRequestParams.put("order_by", order_by);
            filterRequestParams.put("order_type", order_type);
            KwlReturnObject result = accMasterItemsDAOobj.getWarehouseItems(filterRequestParams);
            List list = result.getEntityList();
            Iterator itr = list.iterator();
            JSONArray jArr = new JSONArray();
           
            
            /** 
             * IF QA flow is activated and store is QA store then isQAstore flag set to true otherwise false
             * IF Job workout flow is activated and store is Job workout store then isJobworkoutstore flag set to true otherwise false
             * IF Pick-Pack flow is activated and store is Pick-Pack store then isPickpackstore flag set to true otherwise false.
             */
            
            boolean isQAstore=false;
            boolean isJobworkoutstore=false;
            boolean isPickpackstore=false;
            
            while (itr.hasNext()) {
                InventoryWarehouse inventoryWarehouse = (InventoryWarehouse) itr.next();
                JSONObject obj = new JSONObject();
                obj.put("id", inventoryWarehouse.getId());
                KwlReturnObject res = accountingHandlerDAOobj.getObject(Store.class.getName(), inventoryWarehouse.getId());
                Store store = (Store) res.getEntityList().get(0);
                if (store != null) {
                    obj.put("name", store.getFullName());
                }
                obj.put("parentid", inventoryWarehouse.getParentId() != null ? inventoryWarehouse.getParentId() : "");
                obj.put("parent", inventoryWarehouse.getParent() != null ? inventoryWarehouse.getParent().getName() : "");
                obj.put("locationid", inventoryWarehouse.getLocation() != null ? inventoryWarehouse.getLocation().getId() : "");
                obj.put("location", inventoryWarehouse.getLocation() != null ? inventoryWarehouse.getLocation().getName() : "");
                obj.put("isdefault", inventoryWarehouse.isIsdefault());

                if (extracompanyobj.isActivateMRPModule()) {
                    isQAstore = (extracompanyobj.isActivateQAApprovalFlow() && (extracompanyobj.getInspectionStore() != null)) ? (extracompanyobj.getInspectionStore().equalsIgnoreCase(store.getId())) : false;
                    isJobworkoutstore = (extracompanyobj.isJobWorkOutFlow() && (extracompanyobj.getVendorjoborderstore() != null)) ? (extracompanyobj.getVendorjoborderstore().equalsIgnoreCase(store.getId())) : false;
                    isPickpackstore = (extracompanyobj.isPickpackship() && (extracompanyobj.getPackingstore() != null)) ? (extracompanyobj.getPackingstore().equalsIgnoreCase(store.getId())) : false;
                   
                    /**
                     * if one of the following flag is true then don't put this store into JArr(Don't show this store)
                     * isQAstore
                     * isJobworkoutstore
                     * isPickpackstore.            
                     */
                    
                    /**
                     * if store is SCRAP or REPAIR store then don't put this store into JArr(Don't show this store).                     
                     */
                    
                    if (!(store.getStoreType().ordinal() == StoreType.SCRAP.ordinal()) && !(store.getStoreType().ordinal() == StoreType.REPAIR.ordinal()) && !isQAstore && !isJobworkoutstore && !isPickpackstore) {
                        jArr.put(obj);
                    }
                } else {
                    jArr.put(obj);
                }
            }
            jobj.put("data", jArr);
        } catch (JSONException e) {
            throw ServiceException.FAILURE(e.getMessage(), e);
        }
        return jobj;
    }
    
    @Override
    public JSONObject getLocationItemsFromStore(Map<String, Object> requestParams) throws ServiceException, SessionExpiredException {
        JSONObject jobj = new JSONObject();
         JSONArray jArr = new JSONArray();
        try {
            String storeid="";
            if(requestParams.containsKey("storeid")){
                storeid=(String)requestParams.get("storeid");
            }
            String companyId = "";
            if(requestParams.containsKey(Constants.companyKey)){
                companyId=(String)requestParams.get(Constants.companyKey);
            }
            KwlReturnObject result = accMasterItemsDAOobj.getLocationsFromStore(storeid,companyId);
            List list = result.getEntityList();
            Iterator itr = list.iterator();
            while (itr.hasNext()) {
                String locationId  = (String) itr.next();
                KwlReturnObject locresult = accountingHandlerDAOobj.getObject(InventoryLocation.class.getName(), locationId);
                InventoryLocation inventoryLocation = (InventoryLocation) locresult.getEntityList().get(0);
                if(inventoryLocation!=null){
                    JSONObject obj = new JSONObject();
                    obj.put("id", inventoryLocation.getId());
                    obj.put("name", inventoryLocation.getName());
                    obj.put("parentid", inventoryLocation.getParent() != null ? inventoryLocation.getParent().getId() : "");
                    obj.put("parentid", inventoryLocation.getParent() != null ? inventoryLocation.getParent().getName() : "");
                    obj.put("isdefault", inventoryLocation.isIsdefault());
                    jArr.put(obj);
                }

            }
            jobj.put("data", jArr);
        } catch (JSONException e) {
            throw ServiceException.FAILURE(e.getMessage(), e);
        }
        return jobj;
    }
    
    @Override
        public JSONObject getNewBatches(Map<String, Object> requestParams) throws ServiceException, SessionExpiredException {
        JSONObject jobj = new JSONObject();
        try {
            DateFormat df = (DateFormat) requestParams.get(Constants.df);                             //authHandler.getDateFormatter(request);
            HashMap<String, Object> filterRequestParams = new HashMap<>();
            ArrayList filter_names = new ArrayList(), filter_params = new ArrayList(), order_by = new ArrayList(), order_type = new ArrayList();
            filter_names.add("company.companyID");
            filter_params.add(requestParams.get(Constants.companyKey));
            int transType = 0;
            boolean isOnlyBatchflag = false;
            boolean isOnlyStockflag = false;
            boolean linkflag = false;
            boolean isConsignment = false;
            boolean isEdit = false;
            boolean copyTrans = false;
            String documentid = "";
            if (requestParams.containsKey("isOnlyBatch")) {
                if ((Boolean ) requestParams.get("isOnlyBatch") == true) {
                    isOnlyBatchflag = true;
                }
            }

            if (requestParams.containsKey("isOnlyStkRprt")) {
                if ((Boolean ) requestParams.get("isOnlyStkRprt") == true) {
                    isOnlyBatchflag = true;
                }
            }

            if (requestParams.containsKey("linkflag")) {
                linkflag = (Boolean) requestParams.get("linkflag");  
            }
            if (requestParams.containsKey("isEdit")) {
                isEdit = (Boolean) requestParams.get("isEdit");
            }
            if (requestParams.containsKey("copyTrans")) {
                copyTrans = (Boolean) requestParams.get("copyTrans");
            }

            if (requestParams.containsKey("documentid")) {
                documentid = (String) requestParams.get("documentid");
            }

            if (requestParams.containsKey("location")) {
                filter_names.add("location.id");
                filter_params.add((String) requestParams.get("location"));
            }
            if (requestParams.containsKey("warehouse")) {
                filter_names.add("warehouse.id");
                filter_params.add((String) requestParams.get("warehouse"));
            }


            if (requestParams.containsKey("row")) {
                filter_names.add("row.id");
                filter_params.add((String) requestParams.get("row"));
            }
            if (requestParams.containsKey("rack")) {
                filter_names.add("rack.id");
                filter_params.add((String) requestParams.get("rack"));
            }
            if (requestParams.containsKey("bin")) {
                filter_names.add("bin.id");
                filter_params.add((String) requestParams.get("bin"));
            }

            if (requestParams.containsKey("productid")) {
                filter_names.add("product");
                filter_params.add((String) requestParams.get("productid"));
            }

            if (requestParams.containsKey("ispurchase")) {
                filter_names.add("ispurchase");
                filter_params.add((Boolean) requestParams.get("ispurchase")); 
            } 

            if (requestParams.containsKey("checkbatchname")) {
                filter_names.add("batchname");
                filter_params.add((String) requestParams.get("checkbatchname"));
            }

            if (requestParams.containsKey("isConsignment")) {
                filter_names.add("isconsignment");
                filter_params.add((Boolean) requestParams.get("isConsignment"));
                isConsignment = (Boolean) requestParams.get("isConsignment");
            }

            if (requestParams.containsKey("transType")) {
                transType = (Integer) (requestParams.get("transType"));
                if (transType == Constants.Acc_Delivery_Order_ModuleId || transType == Constants.Acc_Purchase_Return_ModuleId || transType == Constants.Acc_ConsignmentRequest_ModuleId || transType == Constants.Acc_ConsignmentDeliveryOrder_ModuleId) {
                    filter_names.add(">(quantitydue-lockquantity)");
                    filter_params.add(0.0);
                } else if (transType == Constants.Acc_ConsignmentInvoice_ModuleId || transType == Constants.Acc_ConsignmentSalesReturn_ModuleId) {
                    filter_names.add(">(quantitydue-lockquantity)");
                    //                    filter_names.add(">consignquantity");
                    filter_params.add(0.0);
                } else if (transType == Constants.Acc_Sales_Return_ModuleId) {
                    filter_names.add(">(quantity-quantitydue)");
                    filter_params.add(0.0);
                }
            }
            order_by.add("name");
            order_type.add("asc");
            filterRequestParams.put("filter_names", filter_names);
            filterRequestParams.put("filter_params", filter_params);
            filterRequestParams.put("order_by", order_by);
            filterRequestParams.put("order_type", order_type);
            KwlReturnObject result = accMasterItemsDAOobj.getNewBatches(filterRequestParams, isOnlyBatchflag, isOnlyStockflag);



            List list = result.getEntityList();
            Iterator itr = list.iterator();
            if (linkflag && isConsignment) {
                list.removeAll(list); //in link case removed all the previous Batches
            }
            //IN LINK CASE only bring the  serial no of particular document in create new case
//            if (linkflag  && !isEdit && !StringUtil.isNullOrEmpty(documentid)) {
//                KwlReturnObject resultEdit = accMasterItemsDAOobj.getNewSerialsForDocuments(documentid,batchId);
//                List listEdit = resultEdit.getEntityList();
//                Iterator itrBatch = listEdit.iterator();
//                while (itrBatch.hasNext()) {
//                    SerialDocumentMapping serialDocumentMapping = (SerialDocumentMapping) itrBatch.next();
//                    NewBatchSerial batchSerialObj = (NewBatchSerial) kwlCommonTablesDAOObj.getClassObject(NewBatchSerial.class.getName(), serialDocumentMapping.getSerialid().getId());
//                    list.add(batchSerialObj);
//                }
//            }
            JSONArray jArr = new JSONArray();
            while (itr.hasNext()) {
                HashMap<String, Object> filterParamsForPRBatch = new HashMap<String, Object>();
                ArrayList PRfilter_names = new ArrayList(), PRfilter_params = new ArrayList();

                NewProductBatch productBatch = (NewProductBatch) itr.next();
                if (transType == Constants.Acc_Sales_Return_ModuleId) {
                    PRfilter_names.add("ispurchasereturn");
                    PRfilter_params.add(true);
                    PRfilter_names.add("batchmapid.id");
                    PRfilter_params.add(productBatch.getId());
                    filterParamsForPRBatch.put("filter_names", PRfilter_names);
                    filterParamsForPRBatch.put("filter_params", PRfilter_params);
                    KwlReturnObject PRresult = accMasterItemsDAOobj.getPRBatchQuantity(filterParamsForPRBatch);
                    int prCount = PRresult.getEntityList().size();
                    if (prCount == productBatch.getQuantity()) {
                        continue;
                    }
                }
                JSONObject obj = new JSONObject();
                obj.put("id", productBatch.getId());
                obj.put("batch", productBatch.getId());
                obj.put("name", productBatch.getBatchname());
                obj.put("batchname", productBatch.getBatchname());
                obj.put("mfgdate", productBatch.getMfgdate() != null ? df.format(productBatch.getMfgdate()) : "");
                obj.put("expdate", productBatch.getExpdate() != null ? df.format(productBatch.getExpdate()) : "");
                obj.put("warehouse", productBatch.getWarehouse() != null ? productBatch.getWarehouse().getId() : "");
                obj.put("location", productBatch.getLocation() != null ? productBatch.getLocation().getId() : "");
                obj.put("productid", productBatch.getProduct() != null ? productBatch.getProduct() : "");
                jArr.put(obj);
            }
            if (((linkflag && isConsignment) || (isEdit && !copyTrans)) && !StringUtil.isNullOrEmpty(documentid)) {
                filter_names.clear();
                filter_params.clear();
                filter_names.add("locBatchDocMap.documentid");
                filter_params.add(documentid);
                filter_names.add("locBatchDocMap.isconsignment");
                filter_params.add(isConsignment);

                if (transType == Constants.Acc_ConsignmentSalesReturn_ModuleId) {
                    filter_names.add("locBatchDocMap.transactiontype");
                    filter_params.add(28);
                }

                filterRequestParams.put("filter_names", filter_names);
                filterRequestParams.put("filter_params", filter_params);
                KwlReturnObject resultEdit = accMasterItemsDAOobj.getBatchesForDocuments(filterRequestParams);
                List listEdit = resultEdit.getEntityList();
                itr = listEdit.iterator();
                while (itr.hasNext()) {
                    JSONObject obj = new JSONObject();
                    LocationBatchDocumentMapping batchDocumentMapping = (LocationBatchDocumentMapping) itr.next();
                    NewProductBatch productBatch = (NewProductBatch) kwlCommonTablesDAOObj.getClassObject(NewProductBatch.class.getName(), batchDocumentMapping.getBatchmapid().getId());

                    Boolean excludeDuplicate = list.contains(productBatch);
                    double retQty = accMasterItemsDAOobj.getBatcheQuantityForreturn(batchDocumentMapping.getDocumentid(), batchDocumentMapping.getBatchmapid().getId());
                    if (!excludeDuplicate) {
                        if (transType == Constants.Acc_ConsignmentSalesReturn_ModuleId && isConsignment && productBatch != null && ((retQty == batchDocumentMapping.getQuantity())|| productBatch.getQuantitydue() <= 0)) {
                            continue;
                        }
                        obj.put("id", productBatch.getId());
                        obj.put("batch", productBatch.getId());
                        obj.put("name", productBatch.getBatchname());
                        obj.put("batchname", productBatch.getBatchname());
                        obj.put("mfgdate", productBatch.getMfgdate() != null ? df.format(productBatch.getMfgdate()) : "");
                        obj.put("expdate", productBatch.getExpdate() != null ? df.format(productBatch.getExpdate()) : "");
                        obj.put("warehouse", productBatch.getWarehouse() != null ? productBatch.getWarehouse().getId() : "");
                        obj.put("location", productBatch.getLocation() != null ? productBatch.getLocation().getId() : "");
                        obj.put("productid", productBatch.getProduct() != null ? productBatch.getProduct() : "");
                        jArr.put(obj);

                    }
                }
            }
            jobj.put("data", jArr);
        } catch (JSONException e) {
            throw ServiceException.FAILURE(e.getMessage(), e);
        }
        return jobj;
    }
    
    @Override
    public JSONObject getLevels(Map<String, Object> requestParams) throws ServiceException {
        JSONArray dataJArr = new JSONArray();
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        String parent = "";
        try {
            String companyID = (String) requestParams.get(Constants.companyKey);
            Object[] prefObj = null;
            KwlReturnObject presult = accMasterItemsDAOobj.getCompanPreferencesSql(companyID);
            if (presult.getEntityList() != null && !presult.getEntityList().isEmpty()) {
                prefObj = (Object[]) presult.getEntityList().get(0);
            }
            KwlReturnObject result = accMasterItemsDAOobj.getLocationLevelMapping(companyID);
            List<Object> resultList = result.getEntityList();
            if (!resultList.isEmpty()) {
                for (Object resultObj : resultList) {
                    JSONObject jSONObject = new JSONObject();
                    LocationLevelMapping locationLevelmap = (LocationLevelMapping) resultObj;

                    LocationLevel llevel = locationLevelmap.getLlevelid();
                    if (prefObj.length > 0 && (Boolean) prefObj[5]) {
                        if (llevel.getId() == 1) {
                            parent = "0";
                        } else if (llevel.getId() == 2) {
                            parent = "1";
                        } else {
                            parent = locationLevelmap.getParent();
                        }
                    } else {
                        parent = locationLevelmap.getParent();
                    }

                    jSONObject.put("Id", locationLevelmap.getID());
                    jSONObject.put("levelName", llevel.getName());
                    jSONObject.put("newLevelName", locationLevelmap.getNewLevelNm().equals("") ? llevel.getName() : locationLevelmap.getNewLevelNm());
                    jSONObject.put("parent", parent);
                    jSONObject.put("isActivate", locationLevelmap.isActivate());
                    jSONObject.put("levelId", llevel.getId());
                    dataJArr.put(jSONObject);
                }
            } else {
                result = accMasterItemsDAOobj.getLocationLevel();
                resultList = result.getEntityList();
                if (!resultList.isEmpty()) {
                    for (Object resultObj : resultList) {
                        JSONObject jSONObject = new JSONObject();
                        LocationLevel locationLevel = (LocationLevel) resultObj;
                        if (prefObj.length > 0 && (Boolean) prefObj[5]) {
                            if (locationLevel.getId() == 1) {
                                parent = "0";
                            } else if (locationLevel.getId() == 2) {
                                parent = "1";
                            } else {
                                parent = "";
                            }
                        } else {
                            parent = "";
                        }

                        jSONObject.put("Id", locationLevel.getId());
                        jSONObject.put("levelName", locationLevel.getName());
                        jSONObject.put("newLevelName", locationLevel.getName());
                        jSONObject.put("parent", parent);
                        jSONObject.put("isActivate", false);
                        jSONObject.put("levelId", locationLevel.getId());
                        dataJArr.put(jSONObject);
                    }
                }
            }
            jobj.put("data", dataJArr);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("getLevels:", ex);
        } 
        return jobj;
    }
    
    @Override
    public JSONObject getStoreMasters(Map<String, Object> requestParams) throws ServiceException, SessionExpiredException {
        JSONObject jobj = new JSONObject();
        try {
            String gNm=null;
                if(requestParams.containsKey("transType")){
                gNm= (String) requestParams.get("transType");
            }
            int type=0;
            if(gNm!=null){
                
                if(gNm.equals("row")){
                    type=1;
                }else if(gNm.equals("rack")){
                    type=2;
                }else if(gNm.equals("bin")){
                    type=3;
                }
            }
            HashMap<String, Object> filterRequestParams = new HashMap<String, Object>();
            ArrayList filter_names = new ArrayList(), filter_params = new ArrayList(), order_by = new ArrayList(), order_type = new ArrayList();
            filter_names.add("company.companyID");
            filter_names.add("type");
            filter_params.add((String)requestParams.get(Constants.companyKey));
            filter_params.add(type);
            order_by.add("name");
            order_type.add("asc");
            filterRequestParams.put("filter_names", filter_names);
            filterRequestParams.put("filter_params", filter_params);
            filterRequestParams.put("order_by", order_by);
            filterRequestParams.put("order_type", order_type);
            KwlReturnObject result = accMasterItemsDAOobj.getStoreMasters(filterRequestParams);

            List list = result.getEntityList();
            Iterator itr = list.iterator();
            JSONArray jArr = new JSONArray();
            while (itr.hasNext()) {
                StoreMaster storeMaster = (StoreMaster) itr.next();
                JSONObject obj = new JSONObject();
                obj.put("id", storeMaster.getId());
                obj.put("name", storeMaster.getName());
                obj.put("parentid",storeMaster.getParentId());
                jArr.put(obj);
            }
            jobj.put("data", jArr);
        } catch (JSONException e) {
            throw ServiceException.FAILURE(e.getMessage(), e);
        }
        return jobj;
    }
    
    public JSONObject getNewSerials(Map<String, Object> requestMap) throws ServiceException, SessionExpiredException {
        JSONObject jobj = new JSONObject();
        try {
            DateFormat df = authHandler.getDateOnlyFormat();
            HashMap<String, Object> requestParams = new HashMap<>();
            boolean isDO = false;
            boolean isEdit = false;
            boolean fetchPurchasePrice = false;
            boolean duplicatecheck = false;
            boolean copyTrans = false;
            boolean linkflag = false;
            boolean isblokedinso = false;
            boolean isConsignment = false;
            String billid = "";
            String documentid = "";
            String batchId = "";
            int transactionid = 0;
            String companyId = (String) requestMap.get(Constants.companyKey);
            String productid = "";
            
            if(requestMap.containsKey("productid") && requestMap.get("productid") !=null){
                productid=(String)requestMap.get("productid");
            }
                    
            HashMap<String, Object> filterRequestParams = new HashMap<>();
            ArrayList filter_names = new ArrayList(), filter_params = new ArrayList(), order_by = new ArrayList(), order_type = new ArrayList();
            filter_names.add("nbs.company.companyID");
            filter_params.add(companyId);

            if (requestMap.containsKey("batch") && requestMap.get("batch") != null) { 
                filter_names.add("nbs.batch.id");
                filter_params.add(requestMap.get("batch"));
                batchId= (String) requestMap.get("batch");
            }
            if (requestMap.containsKey("checkserialname") && requestMap.get("checkserialname") != null) {  
                filter_names.add("nbs.serialname");
                filter_params.add(requestMap.get("checkserialname"));
            }
            if (requestMap.containsKey("checkbatchname") && requestMap.get("checkbatchname") != null) {
                filter_names.add("nbs.batch.batchname");
                filter_params.add((String)requestMap.get("checkbatchname"));
            }
            if (requestMap.containsKey("isEdit") && requestMap.get("isEdit") != null) {
                isEdit = Boolean.parseBoolean((String) requestMap.get("isEdit"));
            }
            if (requestMap.containsKey("copyTrans") && requestMap.get("copyTrans") != null) {
                copyTrans = Boolean.parseBoolean((String) requestMap.get("copyTrans"));
            }
            if (requestMap.containsKey("linkflag") && requestMap.get("linkflag") != null) {
                linkflag = Boolean.parseBoolean((String) requestMap.get("linkflag"));
            }
            if (requestMap.containsKey("isblokedinso") && requestMap.get("isblokedinso") != null) {
                isblokedinso = Boolean.parseBoolean((String) requestMap.get("isblokedinso"));
            }
            
            if (requestMap.containsKey("documentid") && requestMap.get("documentid") != null) {
                documentid =  (String) requestMap.get("documentid");
            }
            if (requestMap.containsKey("duplicatecheck") && requestMap.get("duplicatecheck") != null) {
                duplicatecheck =  Boolean.parseBoolean((String)requestMap.get("duplicatecheck"));
            }
            if (requestMap.containsKey("fetchPurchasePrice") && requestMap.get("fetchPurchasePrice") != null) {
                fetchPurchasePrice = Boolean.parseBoolean((String)requestMap.get("fetchPurchasePrice"));
            }
//
//            if (requestMap.containsKey("warehouse")) { 
//                filter_names.add("warehouse.id");
//                filter_params.add(requestMap.get("warehouse"));
//            }
            if (!StringUtil.isNullOrEmpty(productid)) {  
                filter_names.add("nbs.product");
                filter_params.add(productid);
            }
            if (requestMap.containsKey("ispurchase") && requestMap.get("ispurchase") != null) {
                filter_names.add("nbs.ispurchase");
                filter_params.add(Boolean.parseBoolean((String) requestMap.get("ispurchase")));
            }
            if (requestMap.containsKey("isConsignment") && requestMap.get("isConsignment") != null) {
                filter_names.add("nbs.isconsignment");
                filter_params.add(Boolean.parseBoolean((String) requestMap.get("isConsignment")));
                isConsignment = Boolean.parseBoolean((String) requestMap.get("isConsignment"));
            }
            
             if (requestMap.containsKey("isForconsignment") && requestMap.get("isForconsignment") != null) {
                filter_names.add("nbs.isForconsignment");
                filter_params.add(Boolean.parseBoolean((String) requestMap.get("isForconsignment")));
            }
//             if (requestMap.containsKey("moduleid")) ) {
//                 int moduleid=Integer.parseInt(requestMap.get("moduleid"));
//                 if(moduleid==Constants.Acc_ConsignmentDeliveryOrder_ModuleId){//Checking For only Consignment Do
//                    filter_names.add("requestApprovalStatus");
//                    filter_params.add(RequestApprovalStatus.APPROVED);
//                 }
//             
//            }
            int transType=99;//set Default value for initialtization
            if (requestMap.containsKey("transType") && ! StringUtil.isNullOrEmpty((String) requestMap.get("transType"))) {
                 transType = Integer.parseInt((String) requestMap.get("transType"));
                if (transType == Constants.Acc_Delivery_Order_ModuleId || transType == Constants.Acc_Purchase_Return_ModuleId || transType==Constants.Acc_ConsignmentRequest_ModuleId || transType==Constants.Acc_ConsignmentDeliveryOrder_ModuleId) {              
                        filter_names.add(">(nbs.quantitydue-nbs.lockquantity)");
                    filter_params.add(0.0);                    
                } else if (transType == Constants.Acc_Sales_Return_ModuleId) {
                    filter_names.add("nbs.quantitydue");
                    filter_params.add(0.0);
                    if (transType == Constants.Acc_Sales_Return_ModuleId) {
                        filter_names.add("nbs.ispurchasereturn");
                        filter_params.add(false);
                    }
                } else if (transType == Constants.Acc_ConsignmentInvoice_ModuleId || transType == Constants.Acc_ConsignmentSalesReturn_ModuleId) {
                    filter_names.add(">(nbs.quantitydue-nbs.lockquantity)");
                    filter_params.add(0.0);
                    if (transType == Constants.Acc_ConsignmentSalesReturn_ModuleId) {
                        filter_names.add("nbs.consignquantity");
                        filter_params.add(1.0);
                    }
                  } else if (transType == Constants.Acc_Lease_DO) {//Checking For only Lease Do
                    filter_names.add(">(nbs.quantitydue)");
                    filter_params.add(0.0);
                  }
                }
            if (requestMap.containsKey("batch") && requestMap.containsKey("location") || requestMap.containsKey("warehouse") || requestMap.containsKey("row") || requestMap.containsKey("rack") || requestMap.containsKey("bin")) {
                if (requestMap.containsKey("warehouse") && requestMap.get("warehouse") != null) {
                    filter_names.add("nbs.batch.warehouse.id");
                    filter_params.add(requestMap.get("warehouse"));
                }
                if (requestMap.containsKey("location") && requestMap.get("location") != null) { 
                    filter_names.add("nbs.batch.location.id");
                    filter_params.add(requestMap.get("location"));
                }
                if (requestMap.containsKey("row") && ! StringUtil.isNullOrEmpty((String) requestMap.get("row"))) {
                    filter_names.add("nbs.batch.row.id");
                    filter_params.add(requestMap.get("row"));
                }
                if (requestMap.containsKey("rack") && ! StringUtil.isNullOrEmpty((String) requestMap.get("rack"))) {
                    filter_names.add("nbs.batch.rack.id");
                    filter_params.add(requestMap.get("rack"));
                }
                if (requestMap.containsKey("bin") && ! StringUtil.isNullOrEmpty((String) requestMap.get("bin"))) {
                    filter_names.add("nbs.batch.bin.id");
                    filter_params.add(requestMap.get("bin"));
                }
            }
            boolean requestRejectedCheck=false;
            if((linkflag && !isEdit) && transType==Constants.Acc_ConsignmentDeliveryOrder_ModuleId && !StringUtil.isNullOrEmpty(documentid) &&  !StringUtil.isNullOrEmpty(batchId)){
               requestRejectedCheck=true;
            }
            
            order_by.add("nbs.exptodate");
            order_type.add("asc");
            filterRequestParams.put("filter_names", filter_names);
            filterRequestParams.put("filter_params", filter_params);
            filterRequestParams.put("order_by", order_by);
            filterRequestParams.put("order_type", order_type);

            KwlReturnObject result = accMasterItemsDAOobj.getNewSerials(filterRequestParams,companyId,productid,batchId,requestRejectedCheck);


            List list = result.getEntityList();
            if((linkflag && !isEdit) && transType==Constants.Acc_ConsignmentDeliveryOrder_ModuleId && !StringUtil.isNullOrEmpty(documentid)){
                 if (linkflag && isblokedinso) {
                    list.removeAll(list); //in link case removed all the previous serial no 
                }
                KwlReturnObject resultEdit = accMasterItemsDAOobj.getNewSerialsForDocuments(documentid,batchId,transType);
                    List listEdit = resultEdit.getEntityList();
                    Iterator itrBatch = listEdit.iterator();
                    while (itrBatch.hasNext()) {
                        SerialDocumentMapping serialDocumentMapping = (SerialDocumentMapping) itrBatch.next();
                        if(serialDocumentMapping!=null ){
                            NewBatchSerial batchSerialObj = (NewBatchSerial) kwlCommonTablesDAOObj.getClassObject(NewBatchSerial.class.getName(), serialDocumentMapping.getSerialid().getId());
                            if(!list.contains(batchSerialObj)){
                                list.add(batchSerialObj);
                            }
                        }                      
                    }
            }else{
                  if (linkflag && !(transType==Constants.Acc_ConsignmentRequest_ModuleId || transType==Constants.Acc_ConsignmentDeliveryOrder_ModuleId || transType==Constants.Acc_Delivery_Order_ModuleId)) {
                    list.removeAll(list); //in link case removed all the previous serial no 
                }
                //IN LINK CASE only bring the  serial no of particular document in create new case
                if (linkflag  && !isEdit && !StringUtil.isNullOrEmpty(documentid)) {
                    KwlReturnObject resultEdit = accMasterItemsDAOobj.getNewSerialsForDocuments(documentid,batchId,transType);
                    List listEdit = resultEdit.getEntityList();
                    Iterator itrBatch = listEdit.iterator();
                    while (itrBatch.hasNext()) {
                        SerialDocumentMapping serialDocumentMapping = (SerialDocumentMapping) itrBatch.next();
                        NewBatchSerial batchSerialObj = (NewBatchSerial) kwlCommonTablesDAOObj.getClassObject(NewBatchSerial.class.getName(), serialDocumentMapping.getSerialid().getId());
                        list.add(batchSerialObj);
                    }
                }
                if (isEdit && duplicatecheck && !StringUtil.isNullOrEmpty(documentid)) {  //in edit case it shoul allow to add exsisting serial no
                    filter_names.clear();
                    filter_params.clear();
                    filter_names.add("nbs.documentid");
                    filter_params.add(documentid);
                    filterRequestParams.put("filter_names", filter_names);
                    filterRequestParams.put("filter_params", filter_params);
                    KwlReturnObject resultEdit = accMasterItemsDAOobj.getSerialsForDocuments(filterRequestParams);
                    List listEdit = resultEdit.getEntityList();
                    Iterator itrBatch = listEdit.iterator();
                    while (itrBatch.hasNext()) {
                        SerialDocumentMapping serialDocumentMapping = (SerialDocumentMapping) itrBatch.next();
                        NewBatchSerial batchSerialObj = (NewBatchSerial) kwlCommonTablesDAOObj.getClassObject(NewBatchSerial.class.getName(), serialDocumentMapping.getSerialid().getId());
                        list.remove(batchSerialObj);
                    }
                }
            }
            
            Iterator itr = list.iterator();
            JSONArray jArr = new JSONArray();
            while (itr.hasNext()) {
                JSONObject obj = new JSONObject();
                NewBatchSerial batchSerial = (NewBatchSerial) itr.next();
                if (fetchPurchasePrice) {
                    HashMap<String, Object> requestParamsSerails = new HashMap<>();
                    requestParamsSerails.put("productid", batchSerial.getProduct());
                    requestParamsSerails.put("serialid", batchSerial.getId());
                    KwlReturnObject rateandQtyResult = accMasterItemsDAOobj.getSerialPurchaseDetails(requestParams);
                    List rateandQtyList = rateandQtyResult.getEntityList();
                    Iterator it = rateandQtyList.iterator();
                    while (it.hasNext()) {
                        Object[] Objrow = (Object[]) it.next();
                        double purchasePrice = (Double) Objrow[4];
                        obj.put("purchaseprice", purchasePrice);
                    }
                }


                obj.put("id", batchSerial.getId());
                obj.put("serialno", batchSerial.getSerialname());
                obj.put("serialnoid", batchSerial.getId());
                obj.put("expstart", batchSerial.getExpfromdate() != null ? df.format(batchSerial.getExpfromdate()) : "");
                obj.put("expend", batchSerial.getExptodate() != null ? df.format(batchSerial.getExptodate()) : "");
                obj.put("purchaseserialid", batchSerial.getId());
                obj.put("purchasebatchid", (batchSerial.getBatch() != null) ? batchSerial.getBatch().getId() : "");
                obj.put("skufield", batchSerial.getSkufield());
                int transTypeCDO = Constants.Acc_ConsignmentSalesReturn_ModuleId;
                String docId = "";


                KwlReturnObject reusablecountobj = accCommonTablesDAO.getSerialsReusableCount(productid, batchSerial.getSerialname(), companyId, transTypeCDO, false, docId, batchSerial.getBatch() != null ? batchSerial.getBatch().getId() : null);
                if (reusablecountobj.getEntityList() != null && !reusablecountobj.getEntityList().isEmpty()) {
                    if (reusablecountobj.getEntityList().get(0) != null) {
                        double sumCount = Double.parseDouble(reusablecountobj.getEntityList().get(0).toString());
                        obj.put("reusablecount", sumCount);
                    } else {
                        obj.put("reusablecount", 0);
                    }
                } else {
                    obj.put("reusablecount", 0);
                }
                jArr.put(obj);

            }
            if (isEdit && !copyTrans && !duplicatecheck && !StringUtil.isNullOrEmpty(documentid)) {
                filter_names.clear();
                filter_params.clear();
                filter_names.add("documentid");
                filter_params.add(documentid);
                if (!StringUtil.isNullOrEmpty(batchId)) {  //for getting the serial of particulat batch
                    filter_names.add("serialid.batch.id");
                    filter_params.add(batchId);
                }
               if(transType==Constants.Acc_ConsignmentDeliveryOrder_ModuleId){ //getting serials of so
                   filter_names.add("transactiontype");
                   filter_params.add(27);
               }
                filterRequestParams.put("filter_names", filter_names);
                filterRequestParams.put("filter_params", filter_params);
                KwlReturnObject resultEdit = accMasterItemsDAOobj.getSerialsForDocuments(filterRequestParams);
                List listEdit = resultEdit.getEntityList();
                itr = listEdit.iterator();
                while (itr.hasNext()) {
                    JSONObject obj = new JSONObject();
                    SerialDocumentMapping serialDocumentMapping = (SerialDocumentMapping) itr.next();
                    NewBatchSerial batchSerial = (NewBatchSerial) kwlCommonTablesDAOObj.getClassObject(NewBatchSerial.class.getName(), serialDocumentMapping.getSerialid().getId());

                    if (fetchPurchasePrice) {
                        HashMap<String, Object> requestParamsSerails = new HashMap<>();
                        requestParamsSerails.put("productid", batchSerial.getProduct());
                        requestParamsSerails.put("serialid", batchSerial.getId());
                        KwlReturnObject rateandQtyResult = accMasterItemsDAOobj.getSerialPurchaseDetails(requestParams);
                        List rateandQtyList = rateandQtyResult.getEntityList();
                        Iterator it = rateandQtyList.iterator();
                        while (it.hasNext()) {
                            Object[] Objrow = (Object[]) it.next();
                            double purchasePrice = (Double) Objrow[4];
                            obj.put("purchaseprice", purchasePrice);
                        }
                    }


                    obj.put("id", batchSerial.getId());
                    obj.put("serialno", batchSerial.getSerialname());
                    obj.put("serialnoid", batchSerial.getId());
                    obj.put("expstart", batchSerial.getExpfromdate() != null ? df.format(batchSerial.getExpfromdate()) : "");
                    obj.put("expend", batchSerial.getExptodate() != null ? df.format(batchSerial.getExptodate()) : "");
                    obj.put("purchaseserialid", batchSerial.getId());
                    obj.put("purchasebatchid", (batchSerial.getBatch() != null) ? batchSerial.getBatch().getId() : "");
                    obj.put("skufield", batchSerial.getSkufield());

                    int transType1 = Constants.Acc_ConsignmentSalesReturn_ModuleId;
                    String docId = "";
                    KwlReturnObject reusablecountobj = accCommonTablesDAO.getSerialsReusableCount(productid, batchSerial.getSerialname(), companyId, transType1, false, docId, batchSerial.getBatch() != null ? batchSerial.getBatch().getId() : null);
                    if (reusablecountobj.getEntityList() != null && !reusablecountobj.getEntityList().isEmpty()) {
                        if (reusablecountobj.getEntityList().get(0) != null) {
                            double sumCount = Double.parseDouble(reusablecountobj.getEntityList().get(0).toString());
                            obj.put("reusablecount", sumCount);
                        } else {
                            obj.put("reusablecount", 0);
                        }
                    } else {
                        obj.put("reusablecount", 0);
                    }
                    jArr.put(obj);
                }
            }


            jobj.put("data", jArr);
        } catch (JSONException e) {
            throw ServiceException.FAILURE(e.getMessage(), e);
        }
        return jobj;
    }
    
   @Override 
    public JSONObject getBatches(HttpServletRequest request) throws ServiceException, SessionExpiredException {
        JSONObject jobj = new JSONObject();
        try {
            HashMap<String, Object> filterRequestParams = new HashMap<String, Object>();
            ArrayList filter_names = new ArrayList(), filter_params = new ArrayList(), order_by = new ArrayList(), order_type = new ArrayList();
            filter_names.add("company.companyID");
            filter_params.add(sessionHandlerImpl.getCompanyid(request));

            if (!StringUtil.isNullOrEmpty(request.getParameter("location"))) {
                filter_names.add("location.id");
                filter_params.add(request.getParameter("location"));
            }
            if (!StringUtil.isNullOrEmpty(request.getParameter("transType"))) {
                filter_names.add("transactiontype");
                filter_params.add(Integer.parseInt(request.getParameter("transType")));
            }
            if (!StringUtil.isNullOrEmpty(request.getParameter("warehouse"))) {
                filter_names.add("warehouse.id");
                filter_params.add(request.getParameter("warehouse"));
            }
            if (!StringUtil.isNullOrEmpty(request.getParameter("productid"))) {
                filter_names.add("product");
                filter_params.add(request.getParameter("productid"));
            }
            if (!StringUtil.isNullOrEmpty(request.getParameter("ispurchase"))) {
                filter_names.add("ispurchase");
                filter_params.add(Boolean.parseBoolean(request.getParameter("ispurchase")));
            }
            if (!StringUtil.isNullOrEmpty(request.getParameter("assetId"))) {
                filter_names.add("asset");
                filter_params.add(request.getParameter("assetId"));
            }

            order_by.add("name");
            order_type.add("asc");
            filterRequestParams.put("filter_names", filter_names);
            filterRequestParams.put("filter_params", filter_params);
            filterRequestParams.put("order_by", order_by);
            filterRequestParams.put("order_type", order_type);
            KwlReturnObject result = accMasterItemsDAOobj.getBatches(filterRequestParams);


            List list = result.getEntityList();
            Iterator itr = list.iterator();
            JSONArray jArr = new JSONArray();
            while (itr.hasNext()) {
                ProductBatch productBatch = (ProductBatch) itr.next();
                JSONObject obj = new JSONObject();
                obj.put("id", productBatch.getId());
                obj.put("batch", productBatch.getId());
                obj.put("name", productBatch.getName());
                obj.put("batchname", productBatch.getName());
                obj.put("mfgdate", productBatch.getMfgdate() != null ? productBatch.getMfgdate() : "");
                obj.put("expdate", productBatch.getExpdate() != null ? productBatch.getExpdate() : "");
                obj.put("warehouse", productBatch.getWarehouse() != null ? productBatch.getWarehouse().getId() : "");
                obj.put("location", productBatch.getLocation() != null ? productBatch.getLocation().getId() : "");
                jArr.put(obj);
            }
            jobj.put("data", jArr);
        } catch (JSONException e) {
            throw ServiceException.FAILURE(e.getMessage(), e);
        }
        return jobj;
    }
      
    @Override
    public JSONObject getNewBatches(JSONObject paramJObj) throws ServiceException, SessionExpiredException {
        JSONObject jobj = new JSONObject();
        try {
            DateFormat df = authHandler.getDateOnlyFormat();
            HashMap<String, Object> filterRequestParams = new HashMap<String, Object>();
            ArrayList filter_names = new ArrayList(), filter_params = new ArrayList(), order_by = new ArrayList(), order_type = new ArrayList();
            filter_names.add("company.companyID");
            filter_params.add(paramJObj.getString(Constants.companyKey));
            int transType = 0, barcodetype = 0;
            boolean isOnlyBatchflag = false;
            boolean isOnlyStockflag = false;
            boolean linkflag = false;
            boolean isConsignment = false;
            boolean isJobworkOrder=false;
            boolean isEdit = false;
            boolean copyTrans = false;
            boolean isSerialForProduct = true;
            boolean isUnbuildAssembly = false;
	    String jobworkorderid="", productCode = "";
            String documentid = "";
            String bomcode = "", producttype="";
            JSONObject jobworkjobj = new JSONObject();
            jobworkjobj.put("companyid", paramJObj.getString(Constants.companyKey));
            if (!StringUtil.isNullOrEmpty(paramJObj.optString("isUnbuildAssembly",null))) {
                isUnbuildAssembly = Boolean.parseBoolean(paramJObj.optString("isUnbuildAssembly"));
            }
            if (!StringUtil.isNullOrEmpty(paramJObj.optString("isOnlyBatch",null))) {
                if ((paramJObj.optString("isOnlyBatch").equals("true"))) {
                    String isOnlyBatch = paramJObj.optString("isOnlyBatch");
                    isOnlyBatchflag = Boolean.parseBoolean(isOnlyBatch);
                }
            }
            if (!StringUtil.isNullOrEmpty(paramJObj.optString("isSerialForProduct",null))) {
                if ((paramJObj.optString("isSerialForProduct").equals("true"))) {
                    String isserial = paramJObj.optString("isSerialForProduct");
                    isSerialForProduct = Boolean.parseBoolean(isserial);
                } else {
                    isSerialForProduct = false;
                }
            } else {
                isSerialForProduct = false;
            }
            if (!StringUtil.isNullOrEmpty(paramJObj.optString("isOnlyStkRprt",null))) {
                if ((paramJObj.optString("isOnlyStkRprt").equals("true"))) {
                    String isOnlyStkRprt =paramJObj.optString("isOnlyStkRprt");
                    isOnlyStockflag = Boolean.parseBoolean(isOnlyStkRprt);
                }
            }
            if (!StringUtil.isNullOrEmpty(paramJObj.optString("linkflag",null))) {
                linkflag = Boolean.parseBoolean(paramJObj.optString("linkflag"));
            }
            if (!StringUtil.isNullOrEmpty(paramJObj.optString("isEdit",null))) {
                isEdit = Boolean.parseBoolean(paramJObj.optString("isEdit"));
            }
            if (!StringUtil.isNullOrEmpty(paramJObj.optString("copyTrans",null))) {
                copyTrans = Boolean.parseBoolean(paramJObj.optString("copyTrans"));
            }
            if (!StringUtil.isNullOrEmpty(paramJObj.optString("documentid",null))) {
                documentid = paramJObj.optString("documentid");
            }
            if (!StringUtil.isNullOrEmpty(paramJObj.optString("jobworkorderid",null))) {
                jobworkorderid = paramJObj.optString("jobworkorderid");
            }
	    if(!StringUtil.isNullOrEmpty(paramJObj.optString("barcodetype", null))) {
                barcodetype = Integer.parseInt(paramJObj.optString("barcodetype"));
                if (!StringUtil.isNullOrEmpty(paramJObj.optString("productcode", null))) {
                    productCode = paramJObj.optString("productcode");
                }
            }            
            if (!StringUtil.isNullOrEmpty(paramJObj.optString("bomid",null))) {
                jobworkjobj.put("bomid", paramJObj.optString("bomid"));
            }
            if (!StringUtil.isNullOrEmpty(paramJObj.optString("producttype",null))) {
                producttype = paramJObj.optString("producttype");
            }
            if (isJobworkOrder) {
                documentid = jobworkorderid;
            }
            if (paramJObj.optString("transType",null) == null || (paramJObj.optString("transType",null) != null && (paramJObj.optString("transType")).equals("29") && isSerialForProduct) || !(paramJObj.optString("transType")).equals("29")) {     //29-salesreturn
                if (!StringUtil.isNullOrEmpty(paramJObj.optString("location",null))) {
                    filter_names.add("location.id");
                    filter_params.add(paramJObj.optString("location"));
                    jobworkjobj.put("locationid", paramJObj.optString("location"));
                }

                if (!StringUtil.isNullOrEmpty(paramJObj.optString("warehouse",null))) {
                    filter_names.add("warehouse.id");
                    filter_params.add(paramJObj.optString("warehouse"));
                    jobworkjobj.put("warehouseid", paramJObj.optString("warehouse"));
                }
                if (!StringUtil.isNullOrEmpty(paramJObj.optString("row",null))) {
                    filter_names.add("row.id");
                    filter_params.add(paramJObj.optString("row"));
                }
                if (!StringUtil.isNullOrEmpty(paramJObj.optString("rack",null))) {
                    filter_names.add("rack.id");
                    filter_params.add(paramJObj.optString("rack"));
                }
                if (!StringUtil.isNullOrEmpty(paramJObj.optString("bin",null))) {
                    filter_names.add("bin.id");
                    filter_params.add(paramJObj.optString("bin"));
                }
            }
            if (!StringUtil.isNullOrEmpty(paramJObj.optString("productid",null))) {
                filter_names.add("product");
                filter_params.add(paramJObj.optString("productid"));
                jobworkjobj.put("productid", paramJObj.optString("productid"));
            }
            if (!StringUtil.isNullOrEmpty(paramJObj.optString("ispurchase",null))) {
                filter_names.add("ispurchase");
                filter_params.add(Boolean.parseBoolean(paramJObj.optString("ispurchase")));
            }
            if (!StringUtil.isNullOrEmpty(paramJObj.optString("checkbatchname",null))) {
                filter_names.add("batchname");
                filter_params.add(paramJObj.optString("checkbatchname"));
            }
            if (!StringUtil.isNullOrEmpty(paramJObj.optString("isConsignment",null))) {
                filter_names.add("isconsignment");
                filter_params.add(Boolean.parseBoolean(paramJObj.optString("isConsignment")));
                isConsignment = Boolean.parseBoolean(paramJObj.optString("isConsignment"));
            }
            if (!StringUtil.isNullOrEmpty(paramJObj.optString("isJobworkOrder",null))) {
                isJobworkOrder = Boolean.parseBoolean(paramJObj.optString("isJobworkOrder"));
            }

            if (!StringUtil.isNullOrEmpty(paramJObj.optString("transType", null))) {
                transType = Integer.parseInt(paramJObj.optString("transType", "1"));
                if (transType == Constants.Acc_Delivery_Order_ModuleId
                        || transType == Constants.Acc_Purchase_Return_ModuleId
                        || transType == Constants.Acc_ConsignmentRequest_ModuleId
                        || transType == Constants.Acc_ConsignmentDeliveryOrder_ModuleId
                        || transType == Constants.Acc_Sales_Order_ModuleId
                        || transType == Constants.Acc_Invoice_ModuleId
                        || transType == Constants.Acc_Cash_Sales_ModuleId
                        || transType == Constants.Acc_ConsignmentInvoice_ModuleId
                        || transType == Constants.Acc_ConsignmentSalesReturn_ModuleId) {
                    if (transType == Constants.Acc_Delivery_Order_ModuleId && !isConsignment) {
                        filter_names.add(">((quantitydue+consignquantity)-lockquantity)");
                        filter_params.add(0.0);
                    } else {
                        filter_names.add(">(quantitydue-lockquantity)");
                        filter_params.add(0.0);
                    }
                } else if (transType == Constants.Acc_Sales_Return_ModuleId && isSerialForProduct) {
                    filter_names.add(">(quantity-quantitydue)");
                    filter_params.add(0.0);
                }
            }
            order_by.add("name");
            order_type.add("asc");
            filterRequestParams.put("filter_names", filter_names);
            filterRequestParams.put("filter_params", filter_params);
            filterRequestParams.put("order_by", order_by);
            filterRequestParams.put("order_type", order_type);
            KwlReturnObject result = null;
            if (producttype.equals("Job Work Assembly") && (transType == Constants.Acc_Delivery_Order_ModuleId || transType == Constants.Acc_Invoice_ModuleId)){
                //Get batch according to bomcode for Job Work assembly product while performing DO transaction and Invoice with DO transaction
                result = accMasterItemsDAOobj.getChallanForJobWorkAssembly(jobworkjobj); 

            } else {
                result = accMasterItemsDAOobj.getNewBatches(filterRequestParams, isOnlyBatchflag, isOnlyStockflag);
            }
            List list = result.getEntityList();
            Iterator itr = list.iterator();

            if ((linkflag && isConsignment) || (linkflag && isJobworkOrder)) {
                list.removeAll(list); //in link case removed all the previous Batches
            }
            JSONArray jArr = new JSONArray();
            while (itr.hasNext()) {
                HashMap<String, Object> filterParamsForPRBatch = new HashMap<String, Object>();
                ArrayList PRfilter_names = new ArrayList(), PRfilter_params = new ArrayList();

                NewProductBatch productBatch = (NewProductBatch) itr.next();
                if (transType == Constants.Acc_Sales_Return_ModuleId) {
                    PRfilter_names.add("ispurchasereturn");
                    PRfilter_params.add(true);
                    PRfilter_names.add("batchmapid.id");
                    PRfilter_params.add(productBatch.getId());
                    filterParamsForPRBatch.put("filter_names", PRfilter_names);
                    filterParamsForPRBatch.put("filter_params", PRfilter_params);
                    KwlReturnObject PRresult = accMasterItemsDAOobj.getPRBatchQuantity(filterParamsForPRBatch);
                    int prCount = PRresult.getEntityList().size();
                    if (prCount == productBatch.getQuantity()) {
                        continue;
                    }
                }
                if (isUnbuildAssembly && productBatch.getQuantitydue() <= 0) {
                    continue;
                }
                JSONObject obj = new JSONObject();
               
                if (paramJObj.optBoolean(Constants.isdefaultHeaderMap, false) == true) {
                    obj.put("purchasebatchid", productBatch.getId());
                    obj.put("purchasebatchidValue", productBatch.getBatchname());
                } else {
                    obj.put("id", productBatch.getId());
                    obj.put("batch", productBatch.getId());
                    obj.put("name", productBatch.getBatchname());
                    obj.put("batchname", productBatch.getBatchname());
		    obj.put("batchid", productBatch.getId());   //ERM-304 For Batch Add Button
                    obj.put("barcodebatch", productCode + " " +productBatch.getBatchname());
                }

                obj.put("mfgdate", productBatch.getMfgdate() != null ? df.format(productBatch.getMfgdate()) : "");
                obj.put("expdate", productBatch.getExpdate() != null ? df.format(productBatch.getExpdate()) : "");
                obj.put("warehouse", productBatch.getWarehouse() != null ? productBatch.getWarehouse().getId() : "");
                obj.put("warehousename", productBatch.getWarehouse() != null ? productBatch.getWarehouse().getName() : "");
                obj.put("location", productBatch.getLocation() != null ? productBatch.getLocation().getId() : "");
                obj.put("locationname", productBatch.getLocation() != null ? productBatch.getLocation().getName() : "");
                obj.put("productid", productBatch.getProduct() != null ? productBatch.getProduct() : "");
                HashMap<String, Object> hashMap = new HashMap<String, Object>();
                hashMap.put("invoiceID", productBatch.getId());
                hashMap.put(Constants.companyKey, productBatch.getCompany().getCompanyID());
                /**
                 * Get document count attached to batch
                 */
                obj.put("attachment", 0);
                obj.put("attachmentids", "");
                KwlReturnObject object = accMasterItemsDAOobj.getBatchDocuments(hashMap);
                if (object.getEntityList() != null && object.getEntityList().size() > 0) {
                    obj.put("attachment", object.getEntityList().size());
                    List<Object[]> attachmentDetails = object.getEntityList();
                    String docids = "";
                    for (Object[] attachmentArray : attachmentDetails) {
                        docids = docids + attachmentArray[3] + ",";
                    }
                    if (!StringUtil.isNullOrEmpty(docids)) {
                        docids = docids.substring(0, docids.length() - 1);
                    }
                    obj.put("attachmentids", docids);
                }
                jArr.put(obj);
            }
            /*
            * if  isJobworkOrder = true then
            * documentid = jobworkorderid
            */
            if (((linkflag && isJobworkOrder) && !StringUtil.isNullOrEmpty(jobworkorderid))) { // here documentid = workjoborderid
                documentid = jobworkorderid;
                filter_names.clear();
                filter_params.clear();
                filter_names.add("locBatchDocMap.documentid");
                filter_params.add(jobworkorderid);
                filterRequestParams.put("filter_names", filter_names);
                filterRequestParams.put("filter_params", filter_params);
                KwlReturnObject resultEdit = accMasterItemsDAOobj.getBatchesForDocuments(filterRequestParams);
                List listEdit = resultEdit.getEntityList();
                itr = listEdit.iterator();
                while (itr.hasNext()) {
                    JSONObject obj = new JSONObject();
                    LocationBatchDocumentMapping batchDocumentMapping = (LocationBatchDocumentMapping) itr.next();
                    NewProductBatch productBatch = (NewProductBatch) kwlCommonTablesDAOObj.getClassObject(NewProductBatch.class.getName(), batchDocumentMapping.getBatchmapid().getId());
                    if(isUnbuildAssembly && productBatch.getQuantitydue()<=0){
                        continue;
                    }
                    Boolean excludeDuplicate = list.contains(productBatch);
                    double retQty = accMasterItemsDAOobj.getBatcheQuantityForreturn(batchDocumentMapping.getDocumentid(), batchDocumentMapping.getBatchmapid().getId());
                    if (!excludeDuplicate) {
                        if (transType == Constants.Acc_ConsignmentSalesReturn_ModuleId && isConsignment && productBatch != null && (productBatch.getQuantitydue() <= 0 || (retQty==batchDocumentMapping.getQuantity()))) {
                            continue;
                        }
                        obj.put("id", productBatch.getId());
                        obj.put("batch", productBatch.getId());
                        obj.put("name", productBatch.getBatchname());
                        obj.put("batchname", productBatch.getBatchname());
                        obj.put("mfgdate", productBatch.getMfgdate() != null ? df.format(productBatch.getMfgdate()) : "");
                        obj.put("expdate", productBatch.getExpdate() != null ? df.format(productBatch.getExpdate()) : "");
                        obj.put("warehouse", productBatch.getWarehouse() != null ? productBatch.getWarehouse().getId() : "");
                        obj.put("location", productBatch.getLocation() != null ? productBatch.getLocation().getId() : "");
                        obj.put("locationname", productBatch.getLocation() != null ? productBatch.getLocation().getName() : "");
                        obj.put("warehousename", productBatch.getWarehouse() != null ? productBatch.getWarehouse().getName() : "");
                        obj.put("productid", productBatch.getProduct() != null ? productBatch.getProduct() : "");
			obj.put("batchid", productBatch.getId());   //ERM-304 For Batch Add Button
                        obj.put("barcodebatch", productCode +" "+ productBatch.getBatchname());
                        jArr.put(obj);

                    }
                }
            }
            if (((linkflag && isConsignment) || (isEdit && !copyTrans)) && !StringUtil.isNullOrEmpty(documentid)) {
                filter_names.clear();
                filter_params.clear();
                filter_names.add("locBatchDocMap.documentid");
                filter_params.add(documentid);
                filter_names.add("locBatchDocMap.isconsignment");
                filter_params.add(isConsignment);
                if (!isConsignment) {
                    if (!StringUtil.isNullOrEmpty(paramJObj.optString("location", null))) {
                        filter_names.add("batchmapid.location.id");
                        filter_params.add(paramJObj.optString("location"));
                    }
                    if (!StringUtil.isNullOrEmpty(paramJObj.optString("warehouse", null))) {
                        filter_names.add("batchmapid.warehouse.id");
                        filter_params.add(paramJObj.optString("warehouse"));
                    }
                }
                if (transType == Constants.Acc_ConsignmentSalesReturn_ModuleId) {
                    filter_names.add("locBatchDocMap.transactiontype");
                    filter_params.add(28);
                }

                filterRequestParams.put("filter_names", filter_names);
                filterRequestParams.put("filter_params", filter_params);
                KwlReturnObject resultEdit = accMasterItemsDAOobj.getBatchesForDocuments(filterRequestParams);
                List listEdit = resultEdit.getEntityList();
                itr = listEdit.iterator();
                while (itr.hasNext()) {
                    JSONObject obj = new JSONObject();
                    LocationBatchDocumentMapping batchDocumentMapping = (LocationBatchDocumentMapping) itr.next();
                    NewProductBatch productBatch = (NewProductBatch) kwlCommonTablesDAOObj.getClassObject(NewProductBatch.class.getName(), batchDocumentMapping.getBatchmapid().getId());
                    if (isUnbuildAssembly && productBatch.getQuantitydue() <= 0) {
                        continue;
                    }
                    Boolean excludeDuplicate = list.contains(productBatch);
                    double retQty = accMasterItemsDAOobj.getBatcheQuantityForreturn(batchDocumentMapping.getDocumentid(), batchDocumentMapping.getBatchmapid().getId());
                    if (!excludeDuplicate) {
                        if (transType == Constants.Acc_ConsignmentSalesReturn_ModuleId && isConsignment && productBatch != null && ((retQty == batchDocumentMapping.getQuantity()) || productBatch.getQuantitydue() <= 0 )) {
                            continue;
                        }
                        
                        if (paramJObj.optBoolean(Constants.isdefaultHeaderMap, false) == true) {
                            obj.put("purchasebatchid", productBatch.getId());
                            obj.put("purchasebatchidValue", productBatch.getBatchname());
                        } else {
                            obj.put("id", productBatch.getId());
                            obj.put("batch", productBatch.getId());
                            obj.put("name", productBatch.getBatchname());
                            obj.put("batchname", productBatch.getBatchname());
			    obj.put("batchid", productBatch.getId());   //ERM-304 For Batch Add Button
                            obj.put("barcodebatch", productCode +" "+ productBatch.getBatchname());
                        }
                       
                        obj.put("mfgdate", productBatch.getMfgdate() != null ? df.format(productBatch.getMfgdate()) : "");
                        obj.put("expdate", productBatch.getExpdate() != null ? df.format(productBatch.getExpdate()) : "");
                        obj.put("warehouse", productBatch.getWarehouse() != null ? productBatch.getWarehouse().getId() : "");
                        obj.put("location", productBatch.getLocation() != null ? productBatch.getLocation().getId() : "");
                        obj.put("locationname", productBatch.getLocation() != null ? productBatch.getLocation().getName() : "");
                        obj.put("warehousename", productBatch.getWarehouse() != null ? productBatch.getWarehouse().getName() : "");
                        obj.put("productid", productBatch.getProduct() != null ? productBatch.getProduct() : "");
                        jArr.put(obj);

                    }
                }
            }
            jobj.put("data", jArr);
        } catch (JSONException e) {
            throw ServiceException.FAILURE(e.getMessage(), e);
        }
        return jobj;
    }
    
    @Override
     public JSONObject getNewSerials(JSONObject paramJobj) throws ServiceException, SessionExpiredException {
        JSONObject jobj = new JSONObject();
        try {
            DateFormat df = authHandler.getDateOnlyFormat();
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            boolean isEdit = false;
            boolean fetchPurchasePrice = false;
            boolean duplicatecheck = false;
             boolean copyTrans = false;
            boolean linkflag = false;
            boolean isLinkedFromPI = false;
            boolean isblokedinso = false;
            boolean ispickpackship=false;
            String documentid = "";
            String batchId = "";
            String storeId="";
            String locationId="";
            String rowId="";
            String rackId="";
            String binId="";
            String docrowid = "";
            String linkedFrom = "";
            String documentIds=null;
            String serialNames = paramJobj.optString("serialNames","");
            String companyId = paramJobj.getString(Constants.companyKey);
            String productid = paramJobj.optString("productid",null);
            String billId = paramJobj.optString("billid",null);
            String stockType = paramJobj.optString("stocktype",null);
            ExtraCompanyPreferences pref = (ExtraCompanyPreferences) kwlCommonTablesDAOObj.getClassObject(ExtraCompanyPreferences.class.getName(), companyId);
            ispickpackship = pref!=null?pref.isPickpackship():false;
            HashMap<String, Object> filterRequestParams = new HashMap<String, Object>();
            ArrayList filter_names = new ArrayList(), filter_params = new ArrayList(), order_by = new ArrayList(), order_type = new ArrayList();
            filter_names.add("nbs.company.companyID");
            filter_params.add(companyId);

            if (!StringUtil.isNullOrEmpty(paramJobj.optString("batch",null))) {
                filter_names.add("nbs.batch.id");
                filter_params.add(paramJobj.optString("batch"));
                batchId=paramJobj.optString("batch");
            }
            if (!StringUtil.isNullOrEmpty(paramJobj.optString("checkserialname",null))) {
                filter_names.add("nbs.serialname");
                filter_params.add(paramJobj.optString("checkserialname"));
            }
            if (!StringUtil.isNullOrEmpty(paramJobj.optString("checkbatchname",null))) {
                filter_names.add("nbs.batch.batchname");
                filter_params.add(paramJobj.optString("checkbatchname"));
            }
            if (!StringUtil.isNullOrEmpty(paramJobj.optString("isEdit",null))) {
                isEdit = Boolean.parseBoolean(paramJobj.optString("isEdit"));
            }
            if (!StringUtil.isNullOrEmpty(paramJobj.optString("copyTrans",null))) {
                copyTrans = Boolean.parseBoolean(paramJobj.optString("copyTrans"));
            }
            if (!StringUtil.isNullOrEmpty(paramJobj.optString("linkflag",null))) {
                linkflag = Boolean.parseBoolean(paramJobj.optString("linkflag"));
            }
            if (!StringUtil.isNullOrEmpty(paramJobj.optString("isLinkedFromPI",null))) {
                isLinkedFromPI = Boolean.parseBoolean(paramJobj.optString("isLinkedFromPI"));
            }
            if (!StringUtil.isNullOrEmpty(paramJobj.optString("isblokedinso",null))) {
                isblokedinso = Boolean.parseBoolean(paramJobj.optString("isblokedinso"));
            }
            
            if (!StringUtil.isNullOrEmpty(paramJobj.optString("documentid",null))) {
                documentid = paramJobj.optString("documentid");
            }
            if (!StringUtil.isNullOrEmpty(paramJobj.optString("docrowid",null))) {
                docrowid = paramJobj.optString("docrowid");
            }
            if (!StringUtil.isNullOrEmpty(paramJobj.optString("linkedFrom",null))) {
                linkedFrom = paramJobj.optString("linkedFrom");
            }
            if (!StringUtil.isNullOrEmpty(paramJobj.optString("duplicatecheck",null))) {
                duplicatecheck =  Boolean.parseBoolean(paramJobj.optString("duplicatecheck"));
            }
            if (!StringUtil.isNullOrEmpty(paramJobj.optString("fetchPurchasePrice",null))) {
                fetchPurchasePrice =  Boolean.parseBoolean(paramJobj.optString("fetchPurchasePrice"));
            }

            if (!StringUtil.isNullOrEmpty(productid)) {
                filter_names.add("nbs.product");
                filter_params.add(productid);
            }
            if (!StringUtil.isNullOrEmpty(paramJobj.optString("ispurchase",null))) {
                filter_names.add("nbs.ispurchase");
                filter_params.add(Boolean.parseBoolean(paramJobj.optString("ispurchase")));
            }
            if (!StringUtil.isNullOrEmpty(paramJobj.optString("isConsignment",null))) {
                filter_names.add("nbs.isconsignment");
                filter_params.add(Boolean.parseBoolean(paramJobj.optString("isConsignment")));
            }
            
             if (!StringUtil.isNullOrEmpty(paramJobj.optString("isForconsignment",null))) {
                filter_names.add("nbs.isForconsignment");
                filter_params.add(Boolean.parseBoolean(paramJobj.optString("isForconsignment")));
            }
            boolean fromVendStock = false;
            int transType = 99;//set Default value for initialtization
            if (!StringUtil.isNullOrEmpty(paramJobj.optString("transType",null))) {
                 transType = Integer.parseInt(paramJobj.optString("transType","1"));
                if (transType == Constants.Acc_Delivery_Order_ModuleId || transType == Constants.Acc_Purchase_Return_ModuleId || transType==Constants.Acc_ConsignmentRequest_ModuleId || transType==Constants.Acc_ConsignmentDeliveryOrder_ModuleId || transType == Constants.Inventory_Stock_Adjustment_ModuleId || transType == Constants.Inventory_ModuleId || transType == Constants.Acc_Stock_Request_ModuleId || transType == Constants.Acc_InterStore_ModuleId  || transType == Constants.Acc_InterLocation_ModuleId || transType == Constants. Acc_Sales_Order_ModuleId || transType == Constants.Acc_Invoice_ModuleId || transType == Constants.Acc_Cash_Sales_ModuleId ) {              
                    if (transType == Constants.Acc_Delivery_Order_ModuleId && "0".equals(stockType)) {
                        filter_names.add(">(nbs.consignquantity-nbs.lockquantity)");
                        filter_params.add(0.0);
                        fromVendStock=true;
                    } else if (transType == Constants.Acc_ConsignmentDeliveryOrder_ModuleId) {
                        filter_names.add(">(nbs.quantitydue)");
                        filter_params.add(0.0);
                    }else {
                        filter_names.add(">(nbs.quantitydue-nbs.lockquantity)");
                    filter_params.add(0.0);                    
                    }
                } else if (transType == Constants.Acc_Sales_Return_ModuleId) {
                    filter_names.add("nbs.quantitydue");
                    filter_params.add(0.0);
                    if (transType == Constants.Acc_Sales_Return_ModuleId) {
                        filter_names.add("nbs.ispurchasereturn");
                        filter_params.add(false);
                    }
                } else if (transType == Constants.Acc_ConsignmentInvoice_ModuleId || transType == Constants.Acc_ConsignmentSalesReturn_ModuleId) {
                    filter_names.add(">(nbs.quantitydue-nbs.lockquantity)");
                    filter_params.add(0.0);
                    if (transType == Constants.Acc_ConsignmentSalesReturn_ModuleId) {
                        filter_names.add("nbs.consignquantity");
                        filter_params.add(1.0);
                    }
                  } else if (transType == Constants.Acc_Lease_DO || transType == Constants.Acc_Stock_Adjustment_ModuleId || transType == Constants.Acc_Stock_Request_ModuleId || transType == Constants.Acc_InterLocation_ModuleId || transType == Constants.Acc_InterStore_ModuleId) {//Checking For only Lease Do
                    filter_names.add(">(nbs.quantitydue)");
                    filter_params.add(0.0);
                  }
                }
            
            if (!StringUtil.isNullOrEmpty(paramJobj.optString("customerID", null)) && transType == Constants.Acc_ConsignmentInvoice_ModuleId) {
                filterRequestParams.put("customerID", paramJobj.optString("customerID", null));
                filterRequestParams.put("productid", productid);
                filterRequestParams.put("company", companyId);
                filterRequestParams.put("custwarehouse", paramJobj.optString("warehouse"));
                documentIds=accMasterItemsDAOobj.getDocumentsIdForInvoice(filterRequestParams);
//                filter_names.add("do.customer");
//                filter_params.add(paramJobj.optString("customerID", null));
            }
            if (StringUtil.isNullOrEmpty(paramJobj.optString("batch",null)) && (!StringUtil.isNullOrEmpty(paramJobj.optString("location",null)) || 
                    !StringUtil.isNullOrEmpty(paramJobj.optString("warehouse",null)) || !StringUtil.isNullOrEmpty(paramJobj.optString("row",null)) 
                    || !StringUtil.isNullOrEmpty(paramJobj.optString("rack",null)) || !StringUtil.isNullOrEmpty(paramJobj.optString("bin",null)))) {
                if (!StringUtil.isNullOrEmpty(paramJobj.optString("warehouse",null))) {
                    filter_names.add("nbs.batch.warehouse.id");
                    filter_params.add(paramJobj.optString("warehouse"));
                    storeId=paramJobj.optString("warehouse");
                }
                if (!StringUtil.isNullOrEmpty(paramJobj.optString("location",null))) {
                    filter_names.add("nbs.batch.location.id");
                    filter_params.add(paramJobj.optString("location"));
                    locationId=paramJobj.optString("location");
                }
                if (!StringUtil.isNullOrEmpty(paramJobj.optString("row",null))) {
                    filter_names.add("nbs.batch.row.id");
                    filter_params.add(paramJobj.optString("row"));
                    rowId=paramJobj.optString("row");
                }
                if (!StringUtil.isNullOrEmpty(paramJobj.optString("rack",null))) {
                    filter_names.add("nbs.batch.rack.id");
                    filter_params.add(paramJobj.optString("rack"));
                    rackId=paramJobj.optString("rack");
                }
                if (!StringUtil.isNullOrEmpty(paramJobj.optString("bin",null))) {
                    filter_names.add("nbs.batch.bin.id");
                    filter_params.add(paramJobj.optString("bin"));
                    binId=paramJobj.optString("bin");
                }
            }
            
            if((transType==Constants.Acc_Purchase_Return_ModuleId ||transType==Constants.Acc_Invoice_ModuleId ||transType==Constants.Acc_Cash_Sales_ModuleId ||transType==Constants.Acc_Delivery_Order_ModuleId) && StringUtil.isNullOrEmpty(batchId) && !StringUtil.isNullOrEmpty(storeId) && !StringUtil.isNullOrEmpty(locationId) && !StringUtil.isNullOrEmpty(productid)){
                NewProductBatch nb=accMasterItemsDAOobj.getERPProductBatch(productid, storeId, locationId, rowId, rackId, binId, batchId);
                batchId=nb.getId();
            }
            boolean requestRejectedCheck=false;
            if((linkflag && !isEdit) && transType==Constants.Acc_ConsignmentDeliveryOrder_ModuleId && !StringUtil.isNullOrEmpty(documentid) &&  !StringUtil.isNullOrEmpty(batchId)){
               requestRejectedCheck=true;
            }
            
            order_by.add("nbs.exptodate");
            order_type.add("asc");
            filterRequestParams.put("filter_names", filter_names);
            filterRequestParams.put("filter_params", filter_params);
            filterRequestParams.put("order_by", order_by);
            filterRequestParams.put("order_type", order_type);
            /**
             * Get Un-Used serials for product.
             */
            KwlReturnObject result = accMasterItemsDAOobj.getNewSerials(filterRequestParams,companyId,productid,batchId,requestRejectedCheck,fromVendStock);


            List list = result.getEntityList();
            if((linkflag && !isEdit) && transType==Constants.Acc_ConsignmentDeliveryOrder_ModuleId && !StringUtil.isNullOrEmpty(documentid)){
                 if (linkflag && isblokedinso) {
                    list.removeAll(list); //in link case removed all the previous serial no 
                }
                KwlReturnObject resultEdit = accMasterItemsDAOobj.getNewSerialsForDocuments(documentid,batchId,transType);
                    List listEdit = resultEdit.getEntityList();
                    Iterator itrBatch = listEdit.iterator();
                    while (itrBatch.hasNext()) {
                        SerialDocumentMapping serialDocumentMapping = (SerialDocumentMapping) itrBatch.next();
                        if(serialDocumentMapping!=null ){
                            NewBatchSerial batchSerialObj = (NewBatchSerial) kwlCommonTablesDAOObj.getClassObject(NewBatchSerial.class.getName(), serialDocumentMapping.getSerialid().getId());
                            if(!list.contains(batchSerialObj)){
                                list.add(batchSerialObj);
                            }
                        }                      
                    }
            }else{
                  if (linkflag && !(transType==Constants.Acc_ConsignmentRequest_ModuleId || transType==Constants.Acc_ConsignmentDeliveryOrder_ModuleId ||
                          transType==Constants.Acc_Delivery_Order_ModuleId || (transType==Constants.Acc_Purchase_Return_ModuleId&&isLinkedFromPI) || (transType == Constants.Acc_Invoice_ModuleId))) {
                    list.removeAll(list); //in link case removed all the previous serial no 
                }
                //IN LINK CASE only bring the  serial no of particular document in create new case
                if (linkflag  && !isEdit && !StringUtil.isNullOrEmpty(documentid)) {
                    KwlReturnObject resultEdit = accMasterItemsDAOobj.getNewSerialsForDocuments(documentid,batchId,transType);
                    List listEdit = resultEdit.getEntityList();
                    Iterator itrBatch = listEdit.iterator();
                    while (itrBatch.hasNext()) {
                        SerialDocumentMapping serialDocumentMapping = (SerialDocumentMapping) itrBatch.next();
                        NewBatchSerial batchSerialObj = (NewBatchSerial) kwlCommonTablesDAOObj.getClassObject(NewBatchSerial.class.getName(), serialDocumentMapping.getSerialid().getId());
                       
                        if(transType==Constants.Acc_Purchase_Return_ModuleId && batchSerialObj.getLockquantity()>0 || (transType==Constants.Acc_Invoice_ModuleId &&!("0".equals(linkedFrom)))){
                            continue;
                        }
                         list.add(batchSerialObj);
                    }
                }
                
                /**
                 * In pick pack ship edit case the current Serials used in the DO being edited must also be displayed during editing that DO.
                 */
                if (isEdit && ispickpackship && !StringUtil.isNullOrEmpty(documentid) && transType==Constants.Acc_Delivery_Order_ModuleId) {
                    KwlReturnObject istserials = accMasterItemsDAOobj.getserialsforPickPackShipDO(documentid, productid);
                    List pickpackserials = istserials.getEntityList();
                    if (!pickpackserials.isEmpty()) {
                        Iterator pickpackitr = pickpackserials.iterator();
                        while (pickpackitr.hasNext()) {
                            NewBatchSerial batchSerialObj = (NewBatchSerial) kwlCommonTablesDAOObj.getClassObject(NewBatchSerial.class.getName(), pickpackitr.next().toString());
                            List<NewBatchSerial> nbobj = new ArrayList<>();
                            nbobj.add(batchSerialObj);
                            NewProductBatch batchobj = (NewProductBatch) kwlCommonTablesDAOObj.getClassObject(NewProductBatch.class.getName(), batchId);
                            String currentbatch = batchSerialObj.getBatch()!=null?batchSerialObj.getBatch().getBatchname():null;
                            if (!StringUtil.isNullOrEmpty(currentbatch)) {//If the current batch matches the one in the DO then insert in the list  
                                if (!list.contains(batchSerialObj) && currentbatch.equals(batchobj.getBatchname())) {
                                    list.add(batchSerialObj);
                                }
                            }
                            /**
                             * If the product has no batches and only serials then insert those serials directly.
                             */
                            else {
                                if (!list.contains(batchSerialObj)) {
                                    list.add(batchSerialObj);
                                }
                            }
                        }   
                    }
                }      
                if (isEdit && duplicatecheck && !StringUtil.isNullOrEmpty(documentid)) {  //in edit case it shoul allow to add exsisting serial no
                    filter_names.clear();
                    filter_params.clear();
                    filter_names.add("nbs.documentid");
                    filter_params.add(documentid);
                    filterRequestParams.put("filter_names", filter_names);
                    filterRequestParams.put("filter_params", filter_params);
                    KwlReturnObject resultEdit = accMasterItemsDAOobj.getSerialsForDocuments(filterRequestParams);
                    List listEdit = resultEdit.getEntityList();
                    Iterator itrBatch = listEdit.iterator();
                    while (itrBatch.hasNext()) {
                        SerialDocumentMapping serialDocumentMapping = (SerialDocumentMapping) itrBatch.next();
                        NewBatchSerial batchSerialObj = (NewBatchSerial) kwlCommonTablesDAOObj.getClassObject(NewBatchSerial.class.getName(), serialDocumentMapping.getSerialid().getId());
                        list.remove(batchSerialObj);
                    }
                }
            }

            if (transType == Constants.Acc_ConsignmentInvoice_ModuleId && !StringUtil.isNullOrEmpty(documentIds) && !StringUtil.isNullOrEmpty(paramJobj.optString("customerID", null))) {
                list.removeAll(list);
                KwlReturnObject resultEdit = accMasterItemsDAOobj.getNewSerialsForDocuments(documentIds, batchId, transType);
                List listEdit = resultEdit.getEntityList();
                Iterator itrBatch = listEdit.iterator();
                while (itrBatch.hasNext()) {
                    SerialDocumentMapping serialDocumentMapping = (SerialDocumentMapping) itrBatch.next();
                    if (serialDocumentMapping != null) {
                        NewBatchSerial batchSerialObj = (NewBatchSerial) kwlCommonTablesDAOObj.getClassObject(NewBatchSerial.class.getName(), serialDocumentMapping.getSerialid().getId());
                        if (!list.contains(batchSerialObj)) {
                            list.add(batchSerialObj);
                        }
                    }
                }
            }
            
            Iterator itr = list.iterator();
            JSONArray jArr = new JSONArray();
            while (itr.hasNext()) {
                JSONObject obj = new JSONObject();
                NewBatchSerial batchSerial = (NewBatchSerial) itr.next();
                if (fetchPurchasePrice) {
                    HashMap<String, Object> requestParamsSerails = new HashMap<String, Object>();
                    requestParamsSerails.put("productid", batchSerial.getProduct());
                    requestParamsSerails.put("serialid", batchSerial.getId());
                    KwlReturnObject rateandQtyResult = accMasterItemsDAOobj.getSerialPurchaseDetails(requestParams);
                    List rateandQtyList = rateandQtyResult.getEntityList();
                    Iterator it = rateandQtyList.iterator();
                    while (it.hasNext()) {
                        Object[] Objrow = (Object[]) it.next();
                        double purchasePrice = (Double) Objrow[4];
                        obj.put("purchaseprice", purchasePrice);
                    }
                }
                

                obj.put("id", batchSerial.getId());
                obj.put("serialnoid", batchSerial.getId());
                obj.put("expstart", batchSerial.getExpfromdate() != null ? df.format(batchSerial.getExpfromdate()) : "");
                obj.put("expend", batchSerial.getExptodate() != null ? df.format(batchSerial.getExptodate()) : "");
                obj.put("purchaseserialid", batchSerial.getId());
                obj.put("purchasebatchid", (batchSerial.getBatch() != null) ? batchSerial.getBatch().getId() : "");
                if (paramJobj.optBoolean(Constants.isdefaultHeaderMap, false) == true) {
                    obj.put("purchaseserialidValue", batchSerial.getSerialname());
                } else {
                    obj.put("serialno", batchSerial.getSerialname());
                }
                obj.put("skufield", batchSerial.getSkufield());
                int transTypeCDO = Constants.Acc_ConsignmentSalesReturn_ModuleId;
                String docId = "";


                KwlReturnObject reusablecountobj = accCommonTablesDAO.getSerialsReusableCount(productid, batchSerial.getSerialname(), companyId, transTypeCDO, false, docId, batchSerial.getBatch() != null ? batchSerial.getBatch().getId() : null);
                if (reusablecountobj.getEntityList() != null && !reusablecountobj.getEntityList().isEmpty()) {
                    if (reusablecountobj.getEntityList().get(0) != null) {
                        double sumCount = Double.parseDouble(reusablecountobj.getEntityList().get(0).toString());
                        obj.put("reusablecount", sumCount);
                    } else {
                        obj.put("reusablecount", 0);
                    }
                } else {
                    obj.put("reusablecount", 0);
                }
                jArr.put(obj);

            }
            if (isEdit && !copyTrans && !duplicatecheck && !StringUtil.isNullOrEmpty(documentid)) {
                
                List<NewBatchSerial> serList=null;
                /**
                 * get serial which are products in Edit case of linking documents case SO-->DO SI-->SI--DO.
                 */
                if (isEdit && ("0".equals(linkedFrom)) && ( transType == Constants.Acc_Invoice_ModuleId || transType == Constants.Acc_Delivery_Order_ModuleId) && !StringUtil.isNullOrEmpty(docrowid)) {

                    JSONObject jsonObj = new JSONObject();
                    jsonObj.put("documentid", documentid);
                    jsonObj.put("docrowid", docrowid);
                    jsonObj.put("batchId", batchId);
                    jsonObj.put(Constants.companyid, companyId);
                    jsonObj.put("isFromSI", "1".equals(linkedFrom) ? true : false);

                    serList = accMasterItemsDAOobj.getExcludeSerials(jsonObj);

                   
                }
                
                filter_names.clear();
                filter_params.clear();
                if (!StringUtil.isNullOrEmpty(batchId)) {  //for getting the serial of particulat batch
                    filterRequestParams.put("batchId", batchId);
                }
                if (transType == Constants.Acc_ConsignmentDeliveryOrder_ModuleId) { //getting serials of so
                    filterRequestParams.put("transactiontype", 27);

                }
                if (transType == Constants.Acc_ConsignmentSalesReturn_ModuleId) { //getting serials of do
                    filterRequestParams.put("transactiontype", 28);
                }
                if (transType == Constants.Acc_Delivery_Order_ModuleId && !StringUtil.isNullOrEmpty(stockType)) {
                    filterRequestParams.put("stocktype", stockType);
                }
                filterRequestParams.put("documentid", documentid);
                filterRequestParams.put("companyid", companyId);
                /**
                 * get serial which used in Document edit case.
                 */
                KwlReturnObject resultEdit = accMasterItemsDAOobj.getSerialsForDocumentsSql(filterRequestParams);
               List listEdit = resultEdit.getEntityList();
               /**
                * Remove duplicate serials in list(Un-used serial list) and listEdit (used serial list).
                */
                if (listEdit!=null && list != null && !list.isEmpty()) {
                    listEdit.removeAll(list);
                }
                if (listEdit!=null && serList != null && !serList.isEmpty()) {
                    listEdit.addAll(serList);
                }
               Set<NewBatchSerial> unionListSet = new HashSet<>();
               if(listEdit!=null && !listEdit.isEmpty()){
                   /**
                     * Remove duplicate serials in listEdit(used serial list) and serList (list of serial from linked document).
                     */
                   unionListSet.addAll(listEdit);
                   itr = unionListSet.iterator();
                   while (itr.hasNext()) {
                       JSONObject obj = new JSONObject();
                    NewBatchSerial batchSerial = (NewBatchSerial) itr.next();
//                    NewBatchSerial batchSerial = (NewBatchSerial) kwlCommonTablesDAOObj.getClassObject(NewBatchSerial.class.getName(), serialDocumentMapping.getSerialid().getId());
                       if (batchSerial.getQuantitydue() == 0 && transType == Constants.Acc_ConsignmentSalesReturn_ModuleId && isEdit) {
                           continue;
                       }
                       /**
                        * removed below code as duplicate serials are already removed using hashset.
                        */
//                    if (serList != null && !serList.isEmpty() && serList.size() > 0 && serList.contains(batchSerial)) {
//                        continue;
//                    }
//                      System.out.println("Available Serials: "+batchSerial.getSerialname()+"\n");
                       if (fetchPurchasePrice) {
                           HashMap<String, Object> requestParamsSerails = new HashMap<String, Object>();
                           requestParamsSerails.put("productid", batchSerial.getProduct());
                           requestParamsSerails.put("serialid", batchSerial.getId());
                           KwlReturnObject rateandQtyResult = accMasterItemsDAOobj.getSerialPurchaseDetails(requestParams);
                           List rateandQtyList = rateandQtyResult.getEntityList();
                           Iterator it = rateandQtyList.iterator();
                           while (it.hasNext()) {
                               Object[] Objrow = (Object[]) it.next();
                               double purchasePrice = (Double) Objrow[4];
                               obj.put("purchaseprice", purchasePrice);
                           }
                       }


                       obj.put("id", batchSerial.getId());
                       obj.put("serialno", batchSerial.getSerialname());
                       obj.put("serialnoid", batchSerial.getId());
                       obj.put("expstart", batchSerial.getExpfromdate() != null ? df.format(batchSerial.getExpfromdate()) : "");
                       obj.put("expend", batchSerial.getExptodate() != null ? df.format(batchSerial.getExptodate()) : "");
                       obj.put("purchaseserialid", batchSerial.getId());
                       obj.put("purchasebatchid", (batchSerial.getBatch() != null) ? batchSerial.getBatch().getId() : "");
                       obj.put("skufield", batchSerial.getSkufield());

                       int transType1 = Constants.Acc_ConsignmentSalesReturn_ModuleId;
                       String docId = "";
                       KwlReturnObject reusablecountobj = accCommonTablesDAO.getSerialsReusableCount(productid, batchSerial.getSerialname(), companyId, transType1, false, docId, batchSerial.getBatch() != null ? batchSerial.getBatch().getId() : null);
                       if (reusablecountobj.getEntityList() != null && !reusablecountobj.getEntityList().isEmpty()) {
                           if (reusablecountobj.getEntityList().get(0) != null) {
                               double sumCount = Double.parseDouble(reusablecountobj.getEntityList().get(0).toString());
                               obj.put("reusablecount", sumCount);
                           } else {
                               obj.put("reusablecount", 0);
                           }
                       } else {
                           obj.put("reusablecount", 0);
                       }
                       jArr.put(obj);

                   }
            }
                if (transType == Constants.Acc_ConsignmentSalesReturn_ModuleId && isEdit) { //getting serials of so
                    HashMap<String, Object> reqPaarams = new HashMap<String, Object>();
                    reqPaarams.put("billid", billId);
                    reqPaarams.put("product", productid);
                    reqPaarams.put("batch", batchId);
                    List srList = accMasterItemsDAOobj.getSerialsForConsignmentEdit(reqPaarams);
                    for (Object obj : srList) {
                        JSONObject jObject = new JSONObject();
                        String serialId = (String) obj;
                        NewBatchSerial batchSerial = (NewBatchSerial) kwlCommonTablesDAOObj.getClassObject(NewBatchSerial.class.getName(), serialId);
                        if (fetchPurchasePrice) {
                            HashMap<String, Object> requestParamsSerails = new HashMap<String, Object>();
                            requestParamsSerails.put("productid", batchSerial.getProduct());
                            requestParamsSerails.put("serialid", batchSerial.getId());
                            KwlReturnObject rateandQtyResult = accMasterItemsDAOobj.getSerialPurchaseDetails(requestParams);
                            List rateandQtyList = rateandQtyResult.getEntityList();
                            Iterator it = rateandQtyList.iterator();
                            while (it.hasNext()) {
                                Object[] Objrow = (Object[]) it.next();
                                double purchasePrice = (Double) Objrow[4];
                                jObject.put("purchaseprice", purchasePrice);
            }
                        }


                        jObject.put("id", batchSerial.getId());
                        if (paramJobj.optBoolean(Constants.isdefaultHeaderMap, false) == true) {
                            jObject.put("purchaseserialidValue", batchSerial.getSerialname());
                        } else {
                            jObject.put("serialno", batchSerial.getSerialname());
                        }
                        jObject.put("serialnoid", batchSerial.getId());
                        jObject.put("expstart", batchSerial.getExpfromdate() != null ? df.format(batchSerial.getExpfromdate()) : "");
                        jObject.put("expend", batchSerial.getExptodate() != null ? df.format(batchSerial.getExptodate()) : "");
                        jObject.put("purchaseserialid", batchSerial.getId());
                        jObject.put("purchasebatchid", (batchSerial.getBatch() != null) ? batchSerial.getBatch().getId() : "");
                        jObject.put("skufield", batchSerial.getSkufield());

                        int transType1 = Constants.Acc_ConsignmentSalesReturn_ModuleId;
                        String docId = "";
                        KwlReturnObject reusablecountobj = accCommonTablesDAO.getSerialsReusableCount(productid, batchSerial.getSerialname(), companyId, transType1, false, docId, batchSerial.getBatch() != null ? batchSerial.getBatch().getId() : null);
                        if (reusablecountobj.getEntityList() != null && !reusablecountobj.getEntityList().isEmpty()) {
                            if (reusablecountobj.getEntityList().get(0) != null) {
                                double sumCount = Double.parseDouble(reusablecountobj.getEntityList().get(0).toString());
                                jObject.put("reusablecount", sumCount);
                            } else {
                                jObject.put("reusablecount", 0);
                            }
                        } else {
                            jObject.put("reusablecount", 0);
                        }
                        jArr.put(jObject);
                    }
                }
                
            }
            
            String warehouse=paramJobj.optString("warehouse");
            String location=paramJobj.optString("location");
            String row=paramJobj.optString("row");
            String rack=paramJobj.optString("rack");
            String transactionid=paramJobj.optString("transactionid");
            String bin=paramJobj.optString("bin");
            String batchName=paramJobj.optString("batchName");
 
            if (!StringUtil.isNullOrEmpty(serialNames) && transactionid.equalsIgnoreCase("854")) {
                jArr=new JSONArray();
                String srl[] = serialNames.split(",");
                for (String s : srl) {
                    s=s.trim();
                    NewProductBatch batchObj = accMasterItemsDAOobj.getERPProductBatch(productid, warehouse, location, row, rack, bin, batchName);
                    NewBatchSerial serialObj = accMasterItemsDAOobj.getERPBatchSerial(productid, batchObj, s,companyId);
                    if (serialObj != null) {
                        JSONObject obj = new JSONObject();
//                                    obj.put("purchaseprice", purchasePrice);
                        obj.put("id", serialObj.getId());
                        obj.put("serialno", serialObj.getSerialname());
                        obj.put("serialnoid", serialObj.getId());
                        obj.put("expstart", serialObj.getExpfromdate() != null ? df.format(serialObj.getExpfromdate()) : "");
                        obj.put("expend", serialObj.getExptodate() != null ? df.format(serialObj.getExptodate()) : "");
                        obj.put("purchaseserialid", serialObj.getId());
                        obj.put("purchasebatchid", (serialObj.getBatch() != null) ? serialObj.getBatch().getId() : "");
                        obj.put("skufield", serialObj.getSkufield());
                        jArr.put(obj);
                    }
                }
            }
            if (!StringUtil.isNullObject(jArr) && jArr.length() > 0) {
                try {
                    JSONArray dataArr = getSortedJsonData(jArr);
                    jArr = (!StringUtil.isNullObject(dataArr)) ? dataArr : jArr;
                } catch (Exception ex) {
                }
            }
            jobj.put("data", jArr);
        } catch (JSONException e) {
            throw ServiceException.FAILURE(e.getMessage(), e);
        }
        return jobj;
    }
    public JSONArray getSortedJsonData(JSONArray jArry) throws ServiceException, JSONException {

//        String jsonArrStr = jArry.toString();
//        JSONArray jsonArr = new JSONArray(jsonArrStr);
        JSONArray sortedJsonArray = new JSONArray();

        try {
            List<JSONObject> jsonValues = new ArrayList<JSONObject>();
            for (int i = 0; i < jArry.length(); i++) {
                jsonValues.add(jArry.getJSONObject(i));
            }
            Collections.sort(jsonValues, new Comparator<JSONObject>() {

                private static final String KEY_NAME = "serialno";

                @Override
                public int compare(JSONObject a, JSONObject b) {
                    String valA = new String();
                    String valB = new String();

                    try {
                        valA = (String) a.get(KEY_NAME);
                        valB = (String) b.get(KEY_NAME);
                    } catch (JSONException e) {
                        //do something
                    }

                    return valA.compareTo(valB);
                    //if you want to change the sort order, simply use the following:
                    //return -valA.compareTo(valB);
                }
            });

            for (int i = 0; i < jArry.length(); i++) {
                sortedJsonArray.put(jsonValues.get(i));
            }
        } catch (Exception e) {
             try {
                throw ServiceException.FAILURE(e.getMessage(), e);
            } catch (ServiceException ex) {
                Logger.getLogger(accMasterItemsController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return sortedJsonArray;
    }

     
    @Override
    public String getParentItemsForMap(FieldParams fieldParams, String parentValueid) throws ServiceException {
        String parentValueArray = "";
        FieldParams fieldParams1 = fieldParams.getParent();
        String commValue = getMasterItemsForCustomIDSCommastr(fieldParams1.getId());
        if (!commValue.isEmpty()) {//If master value of parent is not empty(Parent is also a dimension field)        
            String commValueArray[] = commValue.split(";");
            String parentValueidArray[] = parentValueid.split(",");

            for (int cnt = 0; cnt < commValueArray.length; cnt++) {
                FieldComboData fieldComboDataNew = (FieldComboData) kwlCommonTablesDAOObj.getClassObject(FieldComboData.class.getName(), commValueArray[cnt]);
                // String fieldComboDataNew = commValueArray[cnt];
                for (int parentCnt = 0; parentCnt < parentValueidArray.length; parentCnt++) {
                    try {
                        FieldComboData fieldComboData = (FieldComboData) kwlCommonTablesDAOObj.getClassObject(FieldComboData.class.getName(), parentValueidArray[parentCnt]);
                        if (fieldComboData.getValue().equalsIgnoreCase(fieldComboDataNew.getValue())) {
                            if (parentValueArray.length() > 1) {
                                parentValueArray += "," + fieldComboDataNew.getId();
                            } else {
                                parentValueArray = fieldComboDataNew.getId();
                            }
                        }
                    } catch (ServiceException ex) {
                        Logger.getLogger(accMasterItemsController.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        }

        return parentValueArray;
    }
    /**
     * @Desc : Function to assign parent user group to child  when child is mapped to parent
     * @param reqParams
     * @return
     * @throws ServiceException
     */ 
    @Override
    public String getUserGroupParentForMap(HashMap<String, Object> reqParams) throws ServiceException{
        String parentValueArray = "",name="",chieldValueId="",fieldLabel="";
        //getting the object from user field combo mapping 
        KwlReturnObject result = accMasterItemsDAOobj.getUserGroupmappingId(reqParams);
        Map<String, Object> map = new HashMap<String, Object>();
        String fieldId="";
        JSONObject jsonObj = new JSONObject();
        if(reqParams.containsKey("name")){
            name=(String)reqParams.get("name");
        }
        if(reqParams.containsKey("chieldValueId")){
            chieldValueId=(String)reqParams.get("chieldValueId");
        }
        if(reqParams.containsKey("fieldLabel")){
            fieldLabel=(String)reqParams.get("fieldLabel");
        }
        if(result.getEntityList().size()>0){
     
           UserGroupFieldComboMapping obj=(UserGroupFieldComboMapping)result.getEntityList().get(0);
            try {
                jsonObj.put("companyid", obj.getCompany().getCompanyID());
                jsonObj.put("userGroup", obj.getUsersGroup().getID());
//                jsonObj.put("moduleid",obj.getFieldComboData().getField().getModuleid());
                jsonObj.put("masterVal",name);
                jsonObj.put("fieldname",fieldLabel);
                jsonObj.put("masterItem", chieldValueId);
                saveUserGroupFieldComboMapping(jsonObj);
                
                
            } catch (JSONException ex) {
                Logger.getLogger(AccMasterItemsServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            }
    }        
        return parentValueArray;
    }
     public String getMasterItemsForCustomIDSCommastr(String fieldId) {

        JSONObject jobj = new JSONObject();
        String valuesStr = "";
        boolean issuccess = false;
        try {
            HashMap<String, Object> filterRequestParams = new HashMap<String, Object>();
            ArrayList filter_names = new ArrayList(), filter_params = new ArrayList(), order_by = new ArrayList(), order_type = new ArrayList();
            filter_names.add("field.id");
            filter_params.add(fieldId);
            order_by.add("value");
            order_type.add("asc");
            filterRequestParams.put("filter_names", filter_names);
            filterRequestParams.put("filter_params", filter_params);
            filterRequestParams.put("order_by", order_by);
            filterRequestParams.put("order_type", order_type);
            KwlReturnObject result = accMasterItemsDAOobj.getMasterItemsForCustomHire(filterRequestParams);

            List list = result.getEntityList();
            Iterator itr = list.iterator();
            JSONArray jArr = new JSONArray();
            List ll = result.getEntityList();

            while (itr.hasNext()) {
                Object[] row = (Object[]) itr.next();
                FieldComboData fieldComboData = (FieldComboData) row[0];
                if (itr.hasNext()) {
                    valuesStr += fieldComboData.getId() + ";";
                } else {
                    valuesStr += fieldComboData.getId();
                }
            }
        } catch (Exception e) {
            try {
                throw ServiceException.FAILURE(e.getMessage(), e);
            } catch (ServiceException ex) {
                Logger.getLogger(accMasterItemsController.class.getName()).log(Level.SEVERE, null, ex);
            }
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", "");
            } catch (JSONException ex) {
                Logger.getLogger(accMasterItemsController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return valuesStr;
    }

    /**
     * @Desc : Function to save User Grp
     * @param reqParams
     * @return
     * @throws ServiceException
     */
    public JSONObject saveUserGroup(JSONObject reqParams) throws ServiceException {

        /**
         * Edit case
         */
        Map<String, Object> map = new HashMap();
        map.put("userid", reqParams.optString("userid"));
        String auditMsg="created";
        String usergroupid = reqParams.optString("usergroupid");
        if (!StringUtil.isNullOrEmpty(usergroupid)) {
            /**
             * delete users mapping
             */
            map.put("usergroup", usergroupid);
            accMasterItemsDAOobj.deleteUsersGroupMapping(map);
            auditMsg="updated";
        }

        /**
         * Create Map to save user group
         */
//         map.clear();
        map.put("groupname", reqParams.optString("usergroup"));
        map.put("companyid", reqParams.optString("companyid"));
        KwlReturnObject kwlReturnObject = accMasterItemsDAOobj.addUsersGroup(map);
        UsersGroup usersGroup = null;
        if (kwlReturnObject.getEntityList().size() > 0 && kwlReturnObject.getEntityList() != null && kwlReturnObject.getEntityList().get(0) != null) {
            usersGroup = (UsersGroup) kwlReturnObject.getEntityList().get(0);
            String users = reqParams.optString("users");
            String userArr[] = users.split(",");
            Set<UsersGroupMapping> groupMappings = new HashSet();
            for (String string : userArr) {
                /**
                 * save mapping for each Dim value
                 */
                map.put("userid", string);
                map.put("usergroup", usersGroup.getID());
                KwlReturnObject returnobj = accMasterItemsDAOobj.addUsersGroupMapping(map);
                UsersGroupMapping mapping = returnobj.getEntityList() != null ? (UsersGroupMapping) returnobj.getEntityList().get(0) : null;
                groupMappings.add(mapping);
            }
            map.put("groupMappings", groupMappings);
            accMasterItemsDAOobj.addUsersGroup(map);
        }
        auditTrailObj.insertAuditLog(AuditAction.USER_CREATED, "User " + reqParams.optString(Constants.userfullname) + " has "+auditMsg+" user group " + " " + usersGroup.getName()  + ".", map, usersGroup.getID());

        return reqParams;
    }

    /**
     * @Desc : Function to Get User GRP mapping
     * @param reqParams
     * @return
     * @throws ServiceException
     * @throws JSONException
     */
    public JSONObject getUsersGroup(JSONObject reqParams) throws ServiceException, JSONException {
        /**
         * Create Map to save user group
         */
        JSONObject returnobj = new JSONObject();
        JSONArray dataArr = new JSONArray();
        Map<String, Object> map = new HashMap();
        map.put("companyid", reqParams.optString("companyid"));
        Set<UsersGroupMapping> usersGroupMappings = null;
        KwlReturnObject kwlReturnObject = accMasterItemsDAOobj.getUsersGroup(map);
        List<UsersGroup> usersGroup = kwlReturnObject.getEntityList();
        for (UsersGroup usersGroup1 : usersGroup) {
            JSONObject jSONObject = new JSONObject();
            jSONObject.put("name", usersGroup1.getName());
            StringBuilder stringBuilder = new StringBuilder();
            StringBuilder userids = new StringBuilder();
            usersGroupMappings = usersGroup1.getUsersGroupMappings();
            if (usersGroupMappings != null && usersGroupMappings.size() > 0) {
                for (UsersGroupMapping groupMapping : usersGroupMappings) {
                    stringBuilder.append(groupMapping.getUser().getFirstName()+" " + groupMapping.getUser().getLastName());
                    stringBuilder.append(",");
                    userids.append(groupMapping.getUser().getUserID());
                    userids.append(",");
                }
            }
            jSONObject.put("groupid", usersGroup1.getID());
            jSONObject.put("users", stringBuilder.length() > 1 ? stringBuilder.substring(0, stringBuilder.length() - 1) : "");
            jSONObject.put("usersid", userids.length() > 1 ? userids.substring(0, userids.length() - 1) : "");
            dataArr.put(jSONObject);
        }
        returnobj.put("data", dataArr);
        return returnobj;
    }

    /**
     * @Desc : Function to delete user Grp Mapping
     * @param reqParams
     * @return
     * @throws JSONException
     * @throws ServiceException
     */
    public JSONObject deleteUsersGroup(JSONObject reqParams) throws JSONException, ServiceException {
        String data = reqParams.optString("data");

        JSONArray jSONArray = new JSONArray(data);

        String companyid = reqParams.optString("companyid");
        for (int i = 0; i < jSONArray.length(); i++) {
            JSONObject jobj = jSONArray.getJSONObject(i);
            String groupid = jobj.optString("groupid");
            String groupname = jobj.optString("name");
            Map<String, Object> map = new HashMap();
            map.put("usergroup", groupid);
            map.put("companyid", companyid);
            map.put("userid", reqParams.optString("userid"));
            /**
             * Delete group
             */
            accMasterItemsDAOobj.deleteUsersGroup(map);
            auditTrailObj.insertAuditLog(AuditAction.USER_CREATED, "User " + reqParams.optString(Constants.userfullname) + " has deleted user group " + " " + groupname + ".", map, groupid);
        }
        return reqParams;
    }

    /**
     * @Desc : Function to save User GRP FCD Mapping
     * @param reqParams
     * @return
     * @throws ServiceException
     */
    public JSONObject saveUserGroupFieldComboMapping(JSONObject reqParams) throws ServiceException {
        String masterVal = reqParams.optString("masterVal");
        String companyid = reqParams.optString("companyid");
        String fieldName = reqParams.optString("fieldname");
        String userGroup = reqParams.optString("userGroup");
        String masterItem = reqParams.optString("masterItem");
        HashMap<String, Object> requestParams = new HashMap<String, Object>();
        Map<String, Object> map = new HashMap<String, Object>();
        /**
         * delete existing mapping for grp
         */

        requestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, "fieldlabel"));
        requestParams.put(Constants.filter_values, Arrays.asList(companyid, fieldName));
        /**
         * Is Activated Not Required Because users group Should be updated for all modules
         */
//        requestParams.put("isActivated", 1); // ERP-35222 Unable to Crete COA
        KwlReturnObject result = fieldManagerDAOobj.getFieldParams(requestParams); // get custom field for module
        List<FieldParams> l = result.getEntityList();
        String fieldId = "";
        for (FieldParams tmpcontyp : l) {
            //Removed this check to add functionality to all modules
//            if (tmpcontyp.getModuleid() == Constants.Acc_Product_Master_ModuleId || tmpcontyp.getModuleid() == Constants.Acc_Customer_ModuleId || tmpcontyp.getModuleid() == Constants.Acc_Vendor_ModuleId
//                    || tmpcontyp.getModuleid() == Constants.Account_Statement_ModuleId)
            {
                map.clear();
                map.put("companyid", companyid);
                map.put("userGroup", userGroup);
                map.put("colnum", tmpcontyp.getColnum());
                map.put("moduleid", tmpcontyp.getModuleid());
                fieldId = tmpcontyp.getId();
                if (!StringUtil.isNullOrEmpty(fieldId)) {
                    String ids = fieldManagerDAOobj.getIdsUsingParamsValue(fieldId, masterVal);
                    String[] idsarr = ids.split(",");
                    for (int id = 0; id < idsarr.length; id++) {
                        map.put("fcdid", idsarr[id]);
                        map.put("masterItem", idsarr[id]);
                        accMasterItemsDAOobj.deleteUsersGroupFieldComboMappingUsingFCD(map);
                        accMasterItemsDAOobj.addUsersGroupFCDMapping(map);
                    }
                }

            }
        }

        return reqParams;
    }
    /**
     * @Desc : Function to assign parent user group to child when user is adding user group to parent dimension
     * @param reqParams
     * @return
     * @throws ServiceException
     */
    
    public JSONObject saveUserGroupFieldComboMappingForchild(JSONObject reqParams) throws ServiceException {
        String masterVal = reqParams.optString("masterVal");
        String companyid = reqParams.optString("companyid");
        String fieldName = reqParams.optString("fieldname");
        String userGroup = reqParams.optString("userGroup");
        String masterItem = reqParams.optString("masterItem");
        JSONObject jsonObj = new JSONObject();
        ArrayList masterItemList = new ArrayList();
        HashMap<String, Object> Params = new HashMap<String, Object>();        
        String[] value = masterItem.split(",");
        for(String s:value){
            masterItemList.add(s);
        }
        Params.put("masterItemList", masterItemList);
        //getting object from fieldcombodatamapping if parent contain's child 
        KwlReturnObject result =accMasterItemsDAOobj.getParentMappingToChild(Params);
        List<FieldComboDataMapping> l = result.getEntityList();
        for (FieldComboDataMapping tmpcontyp : l){
            try {
                jsonObj.put("companyid", companyid);
                jsonObj.put("userGroup", userGroup);
//                jsonObj.put("moduleid",obj.getFieldComboData().getField().getModuleid());
                jsonObj.put("masterVal",tmpcontyp.getChild().getValue());
                jsonObj.put("fieldname",tmpcontyp.getChild().getField().getFieldlabel());
                jsonObj.put("masterItem", tmpcontyp.getChild().getId());
                saveUserGroupFieldComboMapping(jsonObj);//saving in usergroupfieldcombomapping entry of child field
                } catch (JSONException ex) {
                Logger.getLogger(AccMasterItemsServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return reqParams;
    }
    public void checkUseInEclaim(HashMap<String, Object> params) throws AccountingException {
        boolean isDeleteDimension = false;
        try {
            String companyid = (String) params.get("companyid");
            String userid = (String) params.get("userid");
            JSONArray syncArray = (JSONArray) params.get("ids");
            if (params != null && params.containsKey("isDeleteDimension")) {
                isDeleteDimension = (Boolean) params.get("isDeleteDimension");
            }
            ServletContext servletContext = (ServletContext) params.get("servletContext");
            //Fetched data from Deskera eClaim
            String action = "805";
            String eclaimURL = servletContext.getInitParameter("eclaimURL");
            JSONObject userData = new JSONObject();
            userData.put("iscommit", true);
            userData.put("remoteapikey", StorageHandler.GetRemoteAPIKey());
            userData.put("companyid", companyid);
            userData.put("userid", userid);
            userData.put("erpcostcenterids", syncArray);

            JSONObject resObj = apiCallHandlerService.callApp(eclaimURL, userData, companyid, action);
            userData = null;

            if (!resObj.isNull("infocode") && !resObj.getBoolean("infocode")) {
                if (isDeleteDimension) {
                    throw new AccountingException("Dimension/Master Items has been used in E-Claim. So you cannot be deleted.");
                } else {
                    throw new AccountingException("The cost center you are trying to delete is already used in eClaim and cannot be deleted.");
                }
            }
        } catch (JSONException ex) {
            Logger.getLogger(accMasterItemsController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }
    }
    
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = {ServiceException.class,JSONException.class, AccountingException.class})
    public JSONObject deleteDimension (JSONObject paramJobj) throws ServiceException, JSONException, AccountingException {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        boolean isParent = false;
        boolean isUsed = false;
        boolean isSameDefaultValue = false;
        boolean isMandatoryField = false;
        String itemIdStr = "";
        String companyid = paramJobj.optString("companyid");
        String moduleIds = paramJobj.getString("moduleIds");
        String comIds[] = moduleIds.split(",");        
        for (int i = 0; i < comIds.length; i++) {
            String grpId = comIds[i];

            HashMap<String, Object> filterRequestParams = new HashMap<String, Object>();
            ArrayList filter_names = new ArrayList(), filter_params = new ArrayList(), order_by = new ArrayList(), order_type = new ArrayList();
            filter_names.add("field.id");
            filter_params.add(grpId);
            order_by.add("value");
            order_type.add("asc");
            filterRequestParams.put("filter_names", filter_names);
            filterRequestParams.put("filter_params", filter_params);
            filterRequestParams.put("order_by", order_by);
            filterRequestParams.put("order_type", order_type);
            FieldParams dimension = (FieldParams) kwlCommonTablesDAOObj.getClassObject(FieldParams.class.getName(), grpId);
            KwlReturnObject result = accMasterItemsDAOobj.getMasterItemsForCustom(filterRequestParams);
            filterRequestParams.clear(); //clear request params from map
            List<FieldComboData> list = result.getEntityList();
            if (list.size() == 0) {
                if (dimension.getIsessential() == 1) {
                    isMandatoryField = true;
                    isSameDefaultValue = accMasterItemsDAOobj.isSameDefaultValueForTransaction(grpId);
                    
                    if (!isSameDefaultValue) {
                        isUsed = true;
                        break;
                    }
                    
                } else {
                    isUsed = accMasterItemsDAOobj.isUsedMasterCustomField(grpId);
                }
                
                if (isUsed) {
                    break;
                }
            }
            for (FieldComboData item : list) {
                itemIdStr += item.getId() + ",";
                if (dimension.getIsessential() == 1) {
                    isMandatoryField = true;
                    isSameDefaultValue = accMasterItemsDAOobj.isSameDefaultValueForTransaction(grpId);

                    if (!isSameDefaultValue) {
                        isUsed = true;
                        break;
                    }

                } else {
                    isUsed = accMasterItemsDAOobj.isUsedMasterCustomItem(item.getId(), grpId);
                }
                if (isUsed) {
                    break;
                }
                //Check whether the associated Master Item used in Eclaim or not. If used then do not delete to its associated Dimension
                if (dimension != null && dimension.getIsforeclaim() == 1) {
                    HashMap<String, Object> params = new HashMap<String, Object>();
                    FieldComboData combo = (FieldComboData) kwlCommonTablesDAOObj.getClassObject(FieldComboData.class.getName(), item.getId());
                    JSONArray jsArr = new JSONArray();
                    JSONObject jsobj = new JSONObject();
                    jsobj.put("eclaimid", combo.getEclaimid());
                    jsobj.put("appuiid", combo.getId());
                    jsArr.put(jsobj);
                    params.put("userid", paramJobj.optString("userid"));
                    params.put("ids", jsArr);
                    params.put("isDeleteDimension", true);
                    params.put("companyid", dimension.getCompanyid());
                    params.put("servletContext", paramJobj.get("servletContext"));
                    checkUseInEclaim(params);
                    params = null;
                }
            }
            list = null;
            if (isUsed) {
                break;
            }
            
            /*
              ERP-39136:[Custom field/Dimension] Mapped parent dimension can be Deleted.
              the following code checks the grpID is present in the database or not.
              If it is present the mapped parent dimension cannot be deleted.
            */
            
            
                filterRequestParams.put("filter_names", Arrays.asList(Constants.companyid, "parentid"));
                filterRequestParams.put("filter_values", Arrays.asList(companyid, grpId));
                KwlReturnObject FieldParamsIDResult = accMasterItemsDAOobj.getFieldParamsUsingSql(filterRequestParams);
                List FieldParamsList = FieldParamsIDResult.getEntityList();
                if (FieldParamsIDResult!=null && FieldParamsList.size()>0) {
                   isUsed=true;
                   isParent=true;
                   filterRequestParams=null;
                  break;
                }  
              filterRequestParams=null;  
        }  
        if (!isUsed) {
            if (isMandatoryField && isSameDefaultValue) {
                // If field is mandatory and has same default value across all modules unlink value from all ttransactions
                
                JSONObject reqParams = new JSONObject();
                KwlReturnObject kwlReturnObject = accMasterItemsDAOobj.getCustomTableName(reqParams);
                List customTableList = kwlReturnObject.getEntityList();
                JSONArray bulkData = accMasterItemsDAOobj.createJsonForCustomTableList(customTableList);
                
                for (int i = 0; i < comIds.length; i++) {
                    String grpId = comIds[i];
                    accMasterItemsDAOobj.unlinkcustomFieldFromTransaction(grpId, bulkData);
                }
            }
            
            String[] itemIdArray = itemIdStr.split(",");
            for (int i = 0; i < itemIdArray.length; i++) {
                accMasterItemsDAOobj.daleteMasterCustomItem(itemIdArray[i]);
            }
            
            /*
            This code is written to get AddressFieldDimensionMapping ID for current Dimension
            */
            JSONObject jSONObject=new JSONObject();
            jSONObject.put("dimension", comIds[0]);
            jSONObject.put("companyid", companyid);
            FieldParams fieldParams=null;
            KwlReturnObject kwlReturnObject1 = accMasterItemsDAOobj.getFieldParamsForDimension(jSONObject); 
            List<FieldParams> fieldParamList = kwlReturnObject1.getEntityList();
            if (fieldParamList.size() > 0) {
                fieldParams = (FieldParams) fieldParamList.get(0);
                jSONObject.put("dimension", fieldParams.getId());
                jSONObject.put("companyid", companyid);
                KwlReturnObject kwlReturnObject = accMasterItemsDAOobj.getAddressMappingForDimension(jSONObject);
                List<AddressFieldDimensionMapping> addressFieldDimensionMappings = kwlReturnObject.getEntityList();
                if (addressFieldDimensionMappings.size() > 0) {
                    AddressFieldDimensionMapping addressFieldDimensionMapping = (AddressFieldDimensionMapping) addressFieldDimensionMappings.get(0);
                    accMasterItemsDAOobj.deleteAddressFieldAgainstDimension(addressFieldDimensionMapping.getId());
                }
            }                                
                        
            for (int i = 0; i < comIds.length; i++) {
                String grpId = comIds[i];
                accMasterItemsDAOobj.deleteDimension(grpId);
                accMasterItemsDAOobj.deleteNotificationRuleOnDimensionDelete(grpId);
            }
            
            String customcolumn = paramJobj.getString("customcolumn");
            boolean iscustom = StringUtil.isNullOrEmpty(paramJobj.getString("iscustom")) ? false : Boolean.parseBoolean(paramJobj.getString("iscustom"));
            String groupname = paramJobj.getString("groupname");
            String action = "dimension";
            String auditaction = (action.equalsIgnoreCase("added") ? AuditAction.DIMENTION_ADDED : AuditAction.DIMENTION_UPDATED);
            if (iscustom == true) {
                if (customcolumn.equals("1")) {
                    action = "custom column";
                    auditaction = (action.equalsIgnoreCase("added") ? AuditAction.CUSTOM_COLUMN_ADDED : AuditAction.CUSTOM_COLUMN_UPDATED);
                } else {
                    action = "custom field";
                    auditaction = (action.equalsIgnoreCase("added") ? AuditAction.CUSTOM_FIELD_ADDED : AuditAction.CUSTOM_FIELD_UPDATED);
                }
            }
            Map<String, Object> map = new HashMap();
            map.put("companyid", companyid);
            map.put("userid", paramJobj.optString("userid"));
            map.put("remoteAddress", paramJobj.optString("remoteAddress"));
            map.put("reqHeader", paramJobj.optString("reqHeader"));
            auditTrailObj.insertAuditLog(auditaction, "User " + paramJobj.optString(Constants.userfullname) + " has deleted " + action + " " + groupname, map, "0");
            map = null;
            
            issuccess = true;
            msg = messageSource.getMessage("acc.dimension.del", null, Locale.forLanguageTag(paramJobj.getString(Constants.language)));
        } else {
            issuccess = true;
            msg = messageSource.getMessage("acc.acc.excp1", null, Locale.forLanguageTag(paramJobj.getString(Constants.language)));
           /*
              following Message is for the mapped parent dimension
            */
            if (isParent) {
                msg = messageSource.getMessage("acc.masterConfig.parentDimension", null, Locale.forLanguageTag(paramJobj.getString(Constants.language)));
            }
        }

        //*********************delete custom field********************************
        ExtraCompanyPreferences pref = (ExtraCompanyPreferences) kwlCommonTablesDAOObj.getClassObject(ExtraCompanyPreferences.class.getName(), companyid);
        boolean propagateTOChildCompaniesFalg = pref.isPropagateToChildCompanies();
        if (propagateTOChildCompaniesFalg) {
            for (int i = 0; i < comIds.length; i++) {
                HashMap<String, Object> filterRequestParams = new HashMap<String, Object>();
                ArrayList filter_names = new ArrayList(), filter_params = new ArrayList();
                filter_names.add("propagatedfieldparamID.id");
                filter_params.add(comIds[i]);
                filterRequestParams.put("filter_names", filter_names);
                filterRequestParams.put("filter_values", filter_params);
                KwlReturnObject kwl = accMasterItemsDAOobj.getFieldParams(filterRequestParams);
                filterRequestParams = null;
                List childList = kwl.getEntityList();
                String fieldidsString = "";
                FieldParams fm = null;
                for (Object ob : childList) {
                    fm = (FieldParams) ob;
                    fieldidsString += fm.getId() + ",";
                }
                childList = null;
                fieldidsString = fieldidsString.substring(0, (fieldidsString.length() - 1));
                deleteDimensionFromChildCompanies(paramJobj, fieldidsString);
            }
        }
        //*****************************************Propagate masteritem In child companies Ends Here**************************
        jobj.put("success", issuccess);
        jobj.put("msg", msg);
        return jobj;
    }
    
    /*
     * This methos is written to verify whether Entity custom data is used in transaction, if so, user cannot change these values against that paricular Entity.
     */
    public JSONObject validateEntityCustomFieldUsage(JSONObject requestJobj) throws JSONException, ServiceException{    
        JSONObject returnObj = new JSONObject();
        JSONObject paramJobj = new JSONObject();
        String companyid = requestJobj.optString("companyid");
        String entityId="",entityValue="";
        JSONObject dimensionConfig = new JSONObject();

        boolean isUsed = false;
        HashMap<String, Object>fieldparams = new HashMap<>();
        if(requestJobj.has("masterItemName") && requestJobj.get("masterItemName")!=null){
            entityValue=String.valueOf(requestJobj.get("masterItemName"));
        }
        fieldparams.put("moduleid", Constants.GSTModule);
        fieldparams.put("companyid", companyid);
            
        paramJobj.put("moduleId", Constants.GSTModule);
        paramJobj.put("companyId", companyid);
        paramJobj.put("fieldName", "Custom_Entity");
        paramJobj.put("fieldValue", entityValue);
                     
        entityId = importDao.getValuesForLinkedRecords(paramJobj); // Retriving Entity id of module 1200, against which custom data is stored in MultiEntityDimesionCustomData
        KwlReturnObject detailsObj = accountingHandlerDAOobj.getObject(MultiEntityDimesionCustomData.class.getName(), entityId);
        MultiEntityDimesionCustomData entityDimesionCustomData = (MultiEntityDimesionCustomData) detailsObj.getEntityList().get(0);
        if (entityDimesionCustomData != null) {

            fieldparams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid, Constants.GST_CONFIG_TYPE));
            fieldparams.put(Constants.filter_values, Arrays.asList(companyid, Constants.GSTModule, Constants.GST_CONFIG_ISFORGST));
            KwlReturnObject kwlReturnObjectGstCust = fieldManagerDAOobj.getFieldParams(fieldparams);        
            List<FieldParams> fieldParamsGstCust = kwlReturnObjectGstCust.getEntityList();
            for (FieldParams fieldParamsObj : fieldParamsGstCust) {
                int colNum = fieldParamsObj.getColnum();
                String entityCustomValue = entityDimesionCustomData.getCol(colNum);
                if (!StringUtil.isNullOrEmpty(entityCustomValue)) {
                    
                    String field = fieldParamsObj.getFieldname();
                    String fieldId = fieldParamsObj.getId();
                    int gstMappingColumn = fieldParamsObj.getGSTMappingColnum();                                                                               
                    paramJobj.put("colNum",colNum);
                    paramJobj.put("gstMappingColumn",gstMappingColumn);
                    paramJobj.put("entityCustomValue",entityCustomValue);
                    paramJobj.put("entityId",entityId);
                    
                    isUsed = accMasterItemsDAOobj.isUsedMasterCustomItemForEntity(paramJobj);
                    if (isUsed) {
                        /*
                         * If field is used against enity then It will be disabled at JS side.
                         */
                        dimensionConfig.put(field, isUsed);
                    }
                }
            }
        }                                                   
        return dimensionConfig;
    }
    
    public void deleteDimensionFromChildCompanies(JSONObject paramJobj, String fieldidsString) throws JSONException, ServiceException, AccountingException {
        boolean isUsed = false;
        String itemIdStr = "";

        String comIds[] = fieldidsString.split(",");
        for (int i = 0; i < comIds.length; i++) {
            String grpId = comIds[i];

            HashMap<String, Object> filterRequestParams = new HashMap<String, Object>();
            ArrayList filter_names = new ArrayList(), filter_params = new ArrayList(), order_by = new ArrayList(), order_type = new ArrayList();
            filter_names.add("field.id");
            filter_params.add(grpId);
            order_by.add("value");
            order_type.add("asc");
            filterRequestParams.put("filter_names", filter_names);
            filterRequestParams.put("filter_params", filter_params);
            filterRequestParams.put("order_by", order_by);
            filterRequestParams.put("order_type", order_type);
            KwlReturnObject result = accMasterItemsDAOobj.getMasterItemsForCustom(filterRequestParams);
            filterRequestParams = null;
            List<FieldComboData> list = result.getEntityList();
            if (list.size() == 0) {
                isUsed = accMasterItemsDAOobj.isUsedMasterCustomField(grpId);
                if (isUsed) {
                    break;
                }
            }
            for (FieldComboData item : list) {
                itemIdStr += item.getId() + ",";
                isUsed = accMasterItemsDAOobj.isUsedMasterCustomItem(item.getId(), grpId);
                if (isUsed) {
                    break;
                }
            }
            list = null;
            if (isUsed) {
                break;
            }
        }
        if (!isUsed) {
            String[] itemIdArray = itemIdStr.split(",");
            for (int i = 0; i < itemIdArray.length; i++) {
                accMasterItemsDAOobj.daleteMasterCustomItem(itemIdArray[i]);
            }
            itemIdArray = null;
            for (int i = 0; i < comIds.length; i++) {
                String grpId = comIds[i];
                accMasterItemsDAOobj.deleteDimension(grpId);
                accMasterItemsDAOobj.deleteNotificationRuleOnDimensionDelete(grpId);
            }
            String customcolumn = paramJobj.getString("customcolumn");
            boolean iscustom = StringUtil.isNullOrEmpty(paramJobj.getString("iscustom")) ? false : Boolean.parseBoolean(paramJobj.getString("iscustom"));
            String groupname = paramJobj.getString("groupname");
            String action = "dimension";
            String auditaction = (action.equalsIgnoreCase("added") ? AuditAction.DIMENTION_ADDED : AuditAction.DIMENTION_UPDATED);
            if (iscustom == true) {
                if (customcolumn.equals("1")) {
                    action = "custom column";
                    auditaction = (action.equalsIgnoreCase("added") ? AuditAction.CUSTOM_COLUMN_ADDED : AuditAction.CUSTOM_COLUMN_UPDATED);
                } else {
                    action = "custom field";
                    auditaction = (action.equalsIgnoreCase("added") ? AuditAction.CUSTOM_FIELD_ADDED : AuditAction.CUSTOM_FIELD_UPDATED);
                }
            }
            Map<String, Object> map = new HashMap();
            map.put("companyid", paramJobj.optString("companyid"));
            map.put("userid", paramJobj.optString("userid"));
            map.put("remoteAddress", paramJobj.optString("remoteAddress"));
            map.put("reqHeader", paramJobj.optString("reqHeader"));
            auditTrailObj.insertAuditLog(auditaction, "User " + paramJobj.optString(Constants.userfullname) + " has deleted  " + action + " " + groupname + " custom field/dimension from child company ", map, "0");
            map = null;
        }
    }
    /**
     * 
     * @param paramJobj     
     * @Desc : This method is for saving custom data of Entity dimension Values     
     */
        public void saveMultiEntityDimCustomDataJSON(JSONObject paramJobj) throws SessionExpiredException, ServiceException, com.krawler.utils.json.base.JSONException {
        JSONObject jobj = new JSONObject();
        List lst = null;       
        HashMap<String, Object> requestParams = new HashMap<String, Object>();        
            String companyid = paramJobj.getString(Constants.companyKey);
            String entityValue = paramJobj.getString("entityvalue");
            String groupname = paramJobj.getString("groupname");            
            String customfield=""; 
            if (paramJobj.get("customfield") != null || paramJobj.get("customfield") != "") {
                customfield = (String) paramJobj.get("customfield");
            }
            String MEDCustomDataId = "";
            MEDCustomDataId = fieldDataManagercntrl.getValuesForLinkRecords(Constants.Acc_Multi_Entity_Dimension_MODULEID, companyid, "Custom_" + groupname, entityValue, 0);            
                HashMap<String, Object> MEDMap = new HashMap<String, Object>();
                JSONArray jcustomarray = new JSONArray(customfield);
                HashMap<String, Object> customrequestParams = new HashMap<String, Object>();
                customrequestParams.put("customarray", jcustomarray);
                customrequestParams.put("modulename", "FieldComboData");
                customrequestParams.put("moduleprimarykey", "FcdId");
                customrequestParams.put("modulerecid", MEDCustomDataId);
                customrequestParams.put(Constants.moduleid, Constants.Acc_Multi_Entity_Dimension_MODULEID);
                customrequestParams.put(Constants.companyKey, companyid);
                customrequestParams.put("customdataclasspath", Constants.Acc_MultiEntityDimension_custom_data_classpath);
                KwlReturnObject customDataresult = fieldDataManagercntrl.setCustomData(customrequestParams);                         
    }
    
    /**
     *  This method retrieves Address Field mapped with Dimension.       
     */ 
    public JSONObject getAddressMappingForDimension(JSONObject jobj) throws JSONException, ServiceException {
        JSONArray jarr = new JSONArray(jobj.get("data").toString());
        JSONObject tempJONObject = new JSONObject(jarr.getJSONObject(0).toString());        
        if (tempJONObject.optBoolean("isForGST")) {
            String dimension = "";
            JSONObject jobj1 = new JSONObject();
            FieldParams fieldParams = null;
            dimension = tempJONObject.optString("id", "");
            String address = "";
            jobj1.put("dimension", dimension);
            jobj1.put("companyid", jobj.optString("companyid"));
//            jobj.remove("companyid");
            if (!String.valueOf(tempJONObject.optString("modulename")).equalsIgnoreCase(Constants.Title_MultiEntityDimension)) {
                KwlReturnObject kwlReturnObject1 = accMasterItemsDAOobj.getFieldParamsForDimension(jobj1); 
                List<FieldParams> fieldParamList = kwlReturnObject1.getEntityList();
                if (fieldParamList.size() > 0) {  // Here required fieldParams ID is of MultiEntityDimension module, if other module id is present then id for MultiEntityDimension field is retrieved 
                    fieldParams = (FieldParams) fieldParamList.get(0);
                    jobj1.put("dimension", (String) fieldParams.getId());
                }
            }
            KwlReturnObject kwlReturnObject = accMasterItemsDAOobj.getAddressMappingForDimension(jobj1);
            List<AddressFieldDimensionMapping> addressFieldDimensionMappings = kwlReturnObject.getEntityList();
            if (addressFieldDimensionMappings.size() > 0) {
                AddressFieldDimensionMapping addressFieldDimensionMapping = (AddressFieldDimensionMapping) addressFieldDimensionMappings.get(0);
                address = addressFieldDimensionMapping.getAddressField();
                if (!StringUtil.isNullOrEmpty(address)) {
                    jobj.put("address", address);
                }
            }
        }
        return jobj;
    }
    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public JSONObject deleteMasterItem(JSONObject paramJobj) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false, isCommitEx = false;
        String msg = "";
        Locale locale = null;
        /*
         * To check Flow For industry code
         */
        boolean industryCodeCheck = false;
        HashMap<String, Object> requestParams = null;
//        TransactionStatus status=null;
        try {
            if (paramJobj.has(Constants.locale)) {
                locale = (Locale) paramJobj.get(Constants.locale);
            }
            String[] idArr = new String[100];
            if (paramJobj.has("ids") && paramJobj.get("ids") != null) {
                String groupString = (String) paramJobj.get("ids");
                idArr = groupString.split(",");
            }
            String[] ids = idArr;
            
            String exceptMasterItems = "", usedInTransaction = "";
            int numRows = 0;
            
            String[] group = new String[100];
            if (paramJobj.has("name") && paramJobj.get("name") != null) {
                String groupString = (String) paramJobj.get("name");
                group = groupString.split(",");
            }
            String[] groupname = group;
            
            String masterGroup = paramJobj.optString("groupname","");
            String companyId = paramJobj.optString("companyid");
            KwlReturnObject extracompanyprefObjresult = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), companyId);
            ExtraCompanyPreferences extracompanyobj = (ExtraCompanyPreferences) extracompanyprefObjresult.getEntityList().get(0);

//                DefaultTransactionDefinition def = new DefaultTransactionDefinition();
//                def.setName("MI_Tx");
//                def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
            
            Map<String, Object> auditRequestParams=new HashMap<String, Object>();
            auditRequestParams.put(Constants.reqHeader, paramJobj.getString(Constants.reqHeader));
            auditRequestParams.put(Constants.remoteIPAddress, paramJobj.getString(Constants.remoteIPAddress));
            auditRequestParams.put(Constants.useridKey, paramJobj.getString(Constants.useridKey));
            
            for (int i = 0; i < ids.length; i++) {
                String returnVal = "";
                try {
                    if (masterGroup.equalsIgnoreCase("Industry Codes")) {
                        /*
                         * To search industry code is already used in product Category
                         */
                        KwlReturnObject resultInd = accMasterItemsDAOobj.getMasterItemsForProductCategory(ids[i], companyId, "19");
                        List listInd = resultInd.getEntityList();
                        /*
                         * To search industry code is already used in Product
                         */
                        KwlReturnObject resultIndProd = accMasterItemsDAOobj.getIndustryCodesMappedWithProduct(ids[i]);
                        List listIndProd = resultIndProd.getEntityList();

                        /*
                         * To search industry code is already used in Multi Entity
                         */
                        KwlReturnObject resultIndEntity = accMasterItemsDAOobj.getIndustryCodesMappedWithMultiEntity(ids[i], companyId);
                        List listIndEntity = resultIndEntity.getEntityList();
                        if ((listInd != null && !listInd.isEmpty()) || (listIndProd != null && !listIndProd.isEmpty()) || (listIndEntity != null && !listIndEntity.isEmpty())) {
                            industryCodeCheck = true;
                            msg = "you cannot delete selected master item as it is already mapped with Product(s)/Product Category/MultiEntity .";
                            throw new AccountingException(msg);
                        }
                    }

//                 status = txnManager.getTransaction(def);
                    KwlReturnObject result = accMasterItemsDAOobj.getSalesPersonMappedWithCustomer(ids[i]);
                    List list = result.getEntityList();
                    if (list != null && !list.isEmpty()) {
//                        jobj.put("success", false);
                        msg =  "Sorry, you cannot delete selected master item as it is already mapped with customer(s).";
                        throw new AccountingException(msg);
                    }
                    if (masterGroup.equalsIgnoreCase("Paid To")) {
                        HashMap<String, Object> ibgBankDetailsMap = new HashMap<String, Object>();
                        ibgBankDetailsMap.put("companyid", companyId);
                        ibgBankDetailsMap.put("masterItemId", ids[i]);
                        accMasterItemsDAOobj.deleteIBGReceivingBankDetails(ibgBankDetailsMap);
                        accMasterItemsDAOobj.deleteCIMBReceivingBankDetails(ibgBankDetailsMap);
                    }
                    if (masterGroup.equalsIgnoreCase(Constants.DRIVER)) {
                        HashMap<String, Object> parameters = new HashMap<>();
                        parameters.put("companyid", companyId);
                        parameters.put("driverid", ids[i]);
                        parameters.put(Constants.locale, locale);
                        returnVal = groupname[i];
                        checkDriverUsedInAnyTransaction(parameters);
                    }
                    /*
                     * Checking whether Sales Person used in any transaction or
                     * not
                     */
                    if (masterGroup.equalsIgnoreCase("Sales Person")) {
                        HashMap<String, Object> parameters = new HashMap<String, Object>();
                        parameters.put("companyid", companyId);
                        parameters.put("masterItemId", ids[i]);
                        returnVal = groupname[i];
                        result = accMasterItemsDAOobj.checkSalesPersonUsedInAnyTransaction(parameters);
                    }

                    if (masterGroup.equalsIgnoreCase("Stock Adjustment Reason")) {
                        KwlReturnObject masterItemResult = accountingHandlerDAOobj.getObject(MasterItem.class.getName(), ids[i]);
                        MasterItem masterItem = (MasterItem) masterItemResult.getEntityList().get(0);
                        if (masterItem != null && masterItem.getDefaultMasterItem() != null && masterItem.getDefaultMasterItem().getID().equalsIgnoreCase(Constants.WASTAGE_ID)) {
                            returnVal = groupname[i];
                            throw new AccountingException("It is default master entry. So cannot deleted.");
                        }
                    }

                    /*
                     * When delete Landing cost category in Master Item it check
                     * it used in transcation .ERP-30190 .
                     */
                    if (masterGroup.equalsIgnoreCase("Landed Cost Category")) {
                        if (ids[i] != null) {
                            KwlReturnObject resultLcc = accMasterItemsDAOobj.getLandingCostCategoryInProduct(ids[i], companyId);
                            List listLcc = resultLcc.getEntityList();
                            if (listLcc != null && !listLcc.isEmpty()) {
                                HashMap<String, Object> itemmap = new HashMap<String, Object>();
                                itemmap.put("id", ids[i]);
                                itemmap.put("companyid", companyId);
                                KwlReturnObject resultLccId = accMasterItemsDAOobj.checkLandingCostCategoryRec(itemmap);
                                List resultLccList = resultLccId.getEntityList();
                                Object[] val = (Object[]) resultLccList.get(0);
                                returnVal = val.length > 1 ? (String) val[1] : "";

                                usedInTransaction = "Sorry, you cannot delete selected master item as it is already mapped with Product.";
                                throw new AccountingException(usedInTransaction);
                            }

                            resultLcc = accMasterItemsDAOobj.getLandingCostCategoryInTranscation(ids[i], companyId);
                            listLcc = resultLcc.getEntityList();
                            if (listLcc != null && !listLcc.isEmpty()) {
                                HashMap<String, Object> itemmap = new HashMap<String, Object>();
                                itemmap.put("id", ids[i]);
                                itemmap.put("companyid", companyId);
                                KwlReturnObject resultLccId = accMasterItemsDAOobj.checkLandingCostCategoryRec(itemmap);
                                List resultLccList = resultLccId.getEntityList();
                                Object[] val = (Object[]) resultLccList.get(0);
                                returnVal = val.length > 1 ? (String) val[1] : "";
                                usedInTransaction = "Sorry, you cannot delete selected master item as it is already mapped with Purchase Invoice(s).";
                                throw new AccountingException(usedInTransaction);
                            } else {
                                KwlReturnObject lccReturnObj = accMasterItemsDAOobj.daleteLandingCostCategoryItem(ids[i], companyId);
                                List lccList = lccReturnObj.getEntityList();
                                if (lccList != null && !lccList.isEmpty()) {
                                    LandingCostCategory lccObj = (LandingCostCategory) lccList.get(0);
                                    returnVal = lccObj.getLccName();
                                }
                            }
                        }
                    }

                    if (masterGroup.equalsIgnoreCase("Sales Person")) {
                        HashMap<String, Object> parameters = new HashMap<String, Object>();
                        parameters.put("companyid", companyId);
                        parameters.put("salespersonid", ids[i]);
                        returnVal = groupname[i];
                        checkSalesPersonUsedInAnyTransaction(parameters);
                    } else if (masterGroup.equalsIgnoreCase("Agent")) {
                        HashMap<String, Object> parameters = new HashMap<String, Object>();
                        parameters.put("companyid", companyId);
                        parameters.put("agentid", ids[i]);
                        returnVal = groupname[i];
                        result = accMasterItemsDAOobj.checkAgentUsedInAnyTransaction(parameters); // Get Agent
                    } else if (masterGroup.equalsIgnoreCase(Constants.WORK_CENTRE_MANAGER) || masterGroup.equalsIgnoreCase(Constants.WORK_CENTRE_LOCATION) || masterGroup.equalsIgnoreCase(Constants.WORK_CENTRE_TYPE) || masterGroup.equalsIgnoreCase(Constants.WORK_TYPE)) {
                        HashMap<String, Object> parameters = new HashMap<String, Object>();
                        parameters.put("companyid", companyId);
                        parameters.put("id", ids[i]);
                        parameters.put("type", masterGroup);
                        returnVal = groupname[i];
                        checkWCMasterItemUsedInAnyWC(parameters); // Get Agent
                    } else if (masterGroup.equalsIgnoreCase(Constants.DELIVERY_ORDER_STATUS)) {  //Check Delivery Order Status used in Transaction
                        requestParams = new HashMap<String, Object>();
                        requestParams.put("companyid", companyId);
                        requestParams.put("statusid", ids[i]);
                        requestParams.put("isdeliveryorder", true);
                        returnVal = groupname[i];
                        accMasterItemsDAOobj.checkDOGRStatusUsedInAnyTransaction(requestParams);

                    } else if (masterGroup.equalsIgnoreCase(Constants.GOODS_RECEIPT_ORDER_STATUS)) {  //Check Goods Receipt Order Status used in Transaction
                        requestParams = new HashMap<String, Object>();
                        requestParams.put("companyid", companyId);
                        requestParams.put("statusid", ids[i]);
                        requestParams.put("isgoodsreceipt", true);
                        returnVal = groupname[i];
                        accMasterItemsDAOobj.checkDOGRStatusUsedInAnyTransaction(requestParams);
                    } else if (masterGroup.equalsIgnoreCase(Constants.VEHICLE_NUMBER)) {
                        requestParams = new HashMap<>();
                        requestParams.put("companyID", companyId);
                        requestParams.put("vehicleNoID", ids[i]);
                        returnVal = groupname[i];
                        accMasterItemsDAOobj.checkVehicleNumberUsedInAnyTransaction(requestParams);
                    } else if (masterGroup.equalsIgnoreCase(Constants.QUALITY_GROUP)) {
                        HashMap<String, Object> parameters = new HashMap<String, Object>();
                        parameters.put("companyid", companyId);
                        parameters.put("id", ids[i]);
                        parameters.put("type", masterGroup);
                        returnVal = groupname[i];
                        checkMasterItemUsedInAnyProductQuality(parameters); // Get Agent
                    } else if (masterGroup.equalsIgnoreCase(Constants.QUALITY_PARAMETER)) { //ERP-25072
                        HashMap<String, Object> parameters = new HashMap<String, Object>();
                        parameters.put("companyid", companyId);
                        parameters.put("id", ids[i]);
                        parameters.put("type", masterGroup);
                        returnVal = groupname[i];
                        checkMasterItemUsedInAnyProductQuality(parameters); //ERP-25072 : Check whether product used in any Product or not
                    } else if (masterGroup.equalsIgnoreCase("Received From")) {
                        requestParams = new HashMap<>();
                        requestParams.put("companyID", companyId);
                        requestParams.put("receivedFromID", ids[i]);
                        returnVal = groupname[i];
                        accMasterItemsDAOobj.checkreceivedFromUsedInAnyTransaction(requestParams);
                    } else if (masterGroup.equalsIgnoreCase("Paid To")) {
                        requestParams = new HashMap<>();
                        requestParams.put("companyID", companyId);
                        requestParams.put("PaidToID", ids[i]);
                        returnVal = groupname[i];
                        accMasterItemsDAOobj.checkpaidToUsedInAnyTransaction(requestParams);
                    } else if (masterGroup.equalsIgnoreCase("Bank Names")) {
                        requestParams = new HashMap<>();
                        requestParams.put("companyID", companyId);
                        requestParams.put("BankNameID", ids[i]);
                        returnVal = groupname[i];
                        accMasterItemsDAOobj.checkBankNameUsedInAnyTransaction(requestParams);
                    } else if (masterGroup.equalsIgnoreCase("Vendor Category")) {
                          returnVal = groupname[i];
                        if (!StringUtil.isNullOrEmpty(ids[i])) {
                            accMasterItemsDAOobj.checkVendorCategoryUsedInAnyTransaction(ids[i]);
                        }
                    } else if (masterGroup.equalsIgnoreCase("Customer Category")){
                         returnVal = groupname[i];
                        if (!StringUtil.isNullOrEmpty(ids[i])) {
                            accMasterItemsDAOobj.checkCustomerCategoryUsedInAnyTransaction(ids[i]);
                        }
                    } else if (masterGroup.equalsIgnoreCase("Reason")) {
                        /**
                         * Check 'Reason' used or not. 
                         */
                        requestParams = new HashMap<>();
                        requestParams.put("companyID", companyId);
                        requestParams.put("ReasonID", ids[i]);
                        returnVal = groupname[i];
                        accMasterItemsDAOobj.checkReasonUsedInAnyTransaction(requestParams);
                    } else if (masterGroup.equalsIgnoreCase("Tax Type")) {
                        requestParams = new HashMap<>();
                        requestParams.put(Constants.companyKey, companyId);
                        requestParams.put(Constants.TAXTYPE, ids[i]);
                        returnVal = groupname[i];
                        accMasterItemsDAOobj.checkTaxTypeUsedInAnyTransaction(requestParams);
                    }
                    if (masterGroup.equalsIgnoreCase("Product Brand") || masterGroup.equalsIgnoreCase("Customer Category")) {
                        if (masterGroup.equalsIgnoreCase("Product Brand")) {
                            HashMap<String, Object> parameters = new HashMap<>();
                            parameters.put("companyID", companyId);
                            parameters.put("productBrandID", ids[i]);
                            returnVal = groupname[i];
                            accMasterItemsDAOobj.checkProductBrandUsedInAnyTransaction(parameters);
                        }

                        // for delete discount rule created
                        HashMap<String, Object> params = new HashMap<>();
                        if (masterGroup.equalsIgnoreCase("Product Brand")) {
                            params.put("productBrandID", ids[i]);
                        } else {
                            params.put("customerCategoryID", ids[i]);
                        }
                        params.put("companyID", companyId);
                        accMasterItemsDAOobj.deleteProductBrandDiscountDetails(params);
                    }
                    /*
                     * Check if any customer bank account type is used in invoice or receiving details
                     */
                    if (masterGroup.equalsIgnoreCase("Customer Bank Account Type")) {
                        HashMap<String, Object> parameters = new HashMap<>();
                        parameters.put("companyID", companyId);
                        parameters.put("customerBankAccountTypeId", ids[i]);
                        returnVal = groupname[i];
                        accMasterItemsDAOobj.checkCustomerBankAccountTypeUsedInAnyTransaction(parameters);
                    }
                    /**
                     * To search Product Category is already tagged to product
                     */
                    if (masterGroup.equalsIgnoreCase("Product Category")) {
                        KwlReturnObject resultIndEntity = accMasterItemsDAOobj.getProductMappedWithProductCategory(ids[i]);
                        List<Object[]> listIndEntity = resultIndEntity.getEntityList();
                        if ((listIndEntity != null && !listIndEntity.isEmpty())) {
                            StringBuilder val = new StringBuilder();
                            for (Object[] row : listIndEntity) {
                                /**
                                 * If one product mapped with multiple categories, should only be named once.
                                 */
                                if (val.indexOf(row[1].toString()) == -1) {
                                    val.append(row[1].toString()).append(", ");
                    }
                            }
                            /**
                             * replace last ", " (comma+space)
                             */
                            val.replace(val.lastIndexOf(", "),val.lastIndexOf(", ")+2 , "");
                            throw new AccountingException(messageSource.getMessage("acc.product.category.alreadyused", new Object[]{val.toString()}, locale));
                        }
                    }
                    /*
                     * checking whether GST_REGISTRATION_TYPE is used in customers or vendores.
                     */
                    if(masterGroup.equalsIgnoreCase(Constants.GST_REGISTRATION_TYPE)){
                        KwlReturnObject resultIndEntity = accMasterItemsDAOobj.getGstRegistrationTypeMappedWithCustomersAndVendors(ids[i]);
                    }
                    /*
                     * checking whether GST_CUSTOMER_VENDOR_TYPE is used in customers or vendores.
                     */
                    if(masterGroup.equalsIgnoreCase(Constants.GST_CUSTOMER_VENDOR_TYPE)){
                        KwlReturnObject resultIndEntity = accMasterItemsDAOobj.getGstCustomerVendorTypeMappedWithCustomersAndVendors(ids[i]);
                    }
                    
                    returnVal = accMasterItemsDAOobj.daleteMasterItem(ids[i]);
                    numRows++;

                    auditTrailObj.insertAuditLog(AuditAction.MASTER_GROUP, "User " + paramJobj.optString(Constants.userfullname) + " has deleted master item " + returnVal + " for master group " + masterGroup, auditRequestParams, "0");
                    if (!Boolean.parseBoolean(StorageHandler.getStandalone()) && !StringUtil.isNullOrEmpty(masterGroup) && masterGroup.equals("Product Category") && extracompanyobj.isIsPOSIntegration()) {
                        //Session session = null;
                        try {

                            String subdomain = paramJobj.optString(Constants.COMPANY_SUBDOMAIN);
                            String posURL = paramJobj.optString(Constants.posURL);
                            String action = "36";
                            //session = HibernateUtil.getCurrentSession();
                            JSONObject resObj = new JSONObject();
                            JSONObject userData = new JSONObject();
                            userData.put("remoteapikey", StorageHandler.GetRemoteAPIKey());
                            userData.put("userid", paramJobj.optString(Constants.userid));
                            userData.put("productcategoryid", ids[i]);
                            userData.put("iscommit", true);
                            userData.put("companyid", companyId);
                            userData.put("subdomain", subdomain);
                            //session = HibernateUtil.getCurrentSession();;
                            resObj = apiCallHandlerService.callApp(posURL, userData, companyId, action);
                        } catch (Exception ex) {
                            Logger.getLogger(accMasterItemsController.class.getName()).log(Level.SEVERE, null, "Prodct Category Delete From ERP To POS- Method Not Found" + ex.getMessage());
                        }
//                        finally {
//                            try {
//                                HibernateUtil.closeSession(session);
//                            } catch (Exception e1) {
//                                Logger.getLogger(accMasterItemsController.class.getName()).log(Level.SEVERE, null, e1.getMessage());
//                            }
//                        }
                    }
//                    txnManager.commit(status);
                } catch (Exception ex) {
                    if (industryCodeCheck) {
                        throw new AccountingException(ex.getMessage());
                    }
                    usedInTransaction = ex.getMessage();
                    isCommitEx = true;
                    exceptMasterItems += returnVal + ", ";
                    if (masterGroup.equalsIgnoreCase("Bank Names") || masterGroup.equalsIgnoreCase("Paid To") || masterGroup.equalsIgnoreCase("Received From") || masterGroup.equalsIgnoreCase("Agent") || masterGroup.equalsIgnoreCase("Sales Person") || masterGroup.equalsIgnoreCase(Constants.GOODS_RECEIPT_ORDER_STATUS) || masterGroup.equalsIgnoreCase(Constants.WORK_CENTRE_MANAGER) || masterGroup.equalsIgnoreCase(Constants.WORK_CENTRE_LOCATION) || masterGroup.equalsIgnoreCase(Constants.WORK_CENTRE_TYPE) || masterGroup.equalsIgnoreCase(Constants.WORK_TYPE) || masterGroup.equalsIgnoreCase(Constants.DELIVERY_ORDER_STATUS) || masterGroup.equalsIgnoreCase(Constants.VEHICLE_NUMBER) || masterGroup.equalsIgnoreCase(Constants.DRIVER)
                            || masterGroup.equalsIgnoreCase(Constants.QUALITY_GROUP) || masterGroup.equalsIgnoreCase("Customer Bank Account Type")) {
                        usedInTransaction = ex.getMessage();
                    }
//                    if (status != null) {
//                        txnManager.rollback(status);
//                    }
                }
            }
            issuccess = true;
            if (StringUtil.isNullOrEmpty(exceptMasterItems)) {
                msg = messageSource.getMessage("acc.master.del", null, StringUtil.getLocale(paramJobj.optString(Constants.language)));  //"Master item has been deleted successfully";
            } else {
                if (ids.length == 1) {
                    msg = usedInTransaction;
                } else {
                    msg = usedInTransaction + " " +messageSource.getMessage("acc.field.MasterItemsexcept", null, StringUtil.getLocale(paramJobj.optString(Constants.language))) + " <b>" + exceptMasterItems.substring(0, exceptMasterItems.length() - 2) + "</b> " + messageSource.getMessage("acc.field.hasbeendeletedsuccessfully", null, StringUtil.getLocale(paramJobj.optString(Constants.language)));
                }
            }
            //***************************
            boolean propagateTOChildCompaniesFalg = false;
            String childCompanyName = "";

            if (!StringUtil.isNullOrEmpty(paramJobj.optString("ispropagatetochildcompanyflag",""))) {
                propagateTOChildCompaniesFalg = Boolean.parseBoolean(paramJobj.optString("ispropagatetochildcompanyflag"));
            }
            if (propagateTOChildCompaniesFalg) {
                for (int i = 0; i < ids.length; i++) {
                    HashMap<String, Object> filterRequestParams = new HashMap<String, Object>();
                    ArrayList filter_names = new ArrayList(), filter_params = new ArrayList();
                    filter_names.add("propagatedMasteritemID.ID");
                    filter_params.add(ids[i]);

                    filterRequestParams.put("filter_names", filter_names);
                    filterRequestParams.put("filter_params", filter_params);
                    KwlReturnObject cntResult = accMasterItemsDAOobj.getMasterItems(filterRequestParams);
                    List childCompaniesMasterItemList = cntResult.getEntityList();

                    for (Object childObj : childCompaniesMasterItemList) {
                        MasterItem childmasteritem = (MasterItem) childObj;
                        try {
                            if (childmasteritem != null) {
//                                status = txnManager.getTransaction(def);
                                String childcompanysmasteritemrid = childmasteritem.getID();
                                String childCompanyID = childmasteritem.getCompany().getCompanyID();
                                childCompanyName = childmasteritem.getCompany().getSubDomain();
                                deleteMasterItemFromChildCompanies(paramJobj, childCompanyID, childcompanysmasteritemrid); // Remove this comment later
//                                    
//                                txnManager.commit(status);
//                                status = null;
                                auditTrailObj.insertAuditLog(AuditAction.MASTER_GROUP, "User " + paramJobj.optString(Constants.userfullname) + " has deleted masteritem" + childmasteritem.getValue() + " from child company " + childCompanyName, auditRequestParams, childmasteritem.getID());
                            }
                        } catch (Exception ex) {
//                            txnManager.rollback(status);
                            auditTrailObj.insertAuditLog(AuditAction.MASTER_GROUP, "User " + paramJobj.optString(Constants.userfullname) + " failed to  delete masteritem" + childmasteritem.getValue() + " from child company " + childCompanyName, auditRequestParams, childmasteritem.getID());
                        }
                    }
                }
            }
            //***********************************

        } catch (Exception ex) {
//            if (status != null&&!industryCodeCheck) {
//                txnManager.rollback(status);
//            }
            msg = "" + ex.getMessage();
            Logger.getLogger(accMasterItemsController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accMasterItemsController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        return jobj;
    }
       public void checkDriverUsedInAnyTransaction(HashMap<String, Object> parameters) throws AccountingException, ServiceException {
        HashMap<String, Object> params = new HashMap<String, Object>();
        Locale locale = null;
        if (parameters.containsKey(Constants.locale)) {
            locale = (Locale) parameters.get(Constants.locale);
        }
        ArrayList filter_names = new ArrayList();
        ArrayList filter_params = new ArrayList();
        filter_names.add("company.companyID");
        filter_params.add(parameters.get("companyid"));
        filter_names.add("driver.ID");
        filter_params.add(parameters.get("driverid"));
        params.put("filter_names", filter_names);
        params.put("filter_params", filter_params);
        KwlReturnObject result1 = accMasterItemsDAOobj.getMasterItems(params);
        List list1 = result1.getEntityList();
        if (list1 != null && !list1.isEmpty()) {
            throw new AccountingException("Master Item is used");   //if Driver used in a vehicle then just do not delete that otherwise constraint voilation exception will occure
        }

        params.clear();
        filter_names.clear();
        filter_params.clear();

        filter_names.add("company");
        filter_params.add(parameters.get("companyid"));
        filter_names.add("driver");
        filter_params.add(parameters.get("driverid"));
        params.put("filter_names", filter_names);
        params.put("filter_values", filter_params);
        result1 = accMasterItemsDAOobj.getDeliveryPlanner(params);
        list1 = result1.getEntityList();
        Iterator countListItr = list1.iterator();

        if (countListItr.hasNext()) {
            int soCnt = Integer.parseInt(countListItr.next().toString());
            if (soCnt > 0) {
                throw new AccountingException("Master Item is used");   //if Driver used in a vehicle then just do not delete that otherwise constraint voilation exception will occure
            }
        }

        params.clear();
        filter_names.clear();
        filter_params.clear();

        filter_names.add("company.companyID");
        filter_params.add(parameters.get("companyid"));
        filter_names.add("driver.ID");
        filter_params.add(parameters.get("driverid"));
        params.put("filter_names", filter_names);
        params.put("filter_params", filter_params);
        result1 = accMasterItemsDAOobj.getCustomerList(params);
        list1 = result1.getEntityList();
        if (list1 != null && !list1.isEmpty()) {
            throw new AccountingException("You can not delete selected master item as it is already used in transaction."); // if Driver used in a Customer then just do not delete that otherwise constraint voilation exception will occure
        }

           params.clear();
           filter_names.clear();
           filter_params.clear();

           filter_names.add("company.companyID");
           filter_params.add(parameters.get(Constants.companyKey));
           filter_names.add("driver.ID");
           filter_params.add(parameters.get(Constants.driverId));
           params.put(Constants.filterNamesKey, filter_names);
           params.put(Constants.filterParamsKey, filter_params);
           result1 = accMasterItemsDAOobj.getDeliveryOrderList(params);
           list1 = result1.getEntityList();
           if (list1 != null && !list1.isEmpty()) {
               throw new AccountingException(messageSource.getMessage("acc.masteritem.driver.cannotdelete", null, locale));
           }
    }
    public void checkMasterItemUsedInAnyProductQuality(HashMap<String, Object> parameters) throws AccountingException, ServiceException {
        if (parameters.containsKey("companyid") && parameters.get("companyid") != null) {
            KwlReturnObject result = accMasterItemsDAOobj.getQualityByWCMasterItem(parameters); // Get Sales Order by Sales Person
            List list1 = result.getEntityList();
            int count1 = list1.size();
            if (count1 > 0) {
                throw new AccountingException("You cannot delete selected Master Item as it is already used in transaction(s).");
            }
        }
    }
    public void checkSalesPersonUsedInAnyTransaction(HashMap<String, Object> parameters) throws AccountingException, ServiceException {
        if (parameters.containsKey("companyid") && parameters.get("companyid") != null) {
            KwlReturnObject result = accMasterItemsDAOobj.getSalesOrdersBySalesPerson(parameters); // Get Sales Order by Sales Person
            List list1 = result.getEntityList();
            int count1 = list1.size();

            result = accMasterItemsDAOobj.getCustomerQuotationsBySalesPerson(parameters);  // Get Customer Quotation by Sales Person
            List list2 = result.getEntityList();
            int count2 = list2.size();

            result = accMasterItemsDAOobj.getSalesInvoicesBySalesPerson(parameters); // Get Sales Invoice by Sales Person
            List list3 = result.getEntityList();
            int count3 = list3.size();

            result = accMasterItemsDAOobj.getDeliveryOrdersBySalesPerson(parameters); // Get Delivery Order by Sales Person
            List list4 = result.getEntityList();
            int count4 = list4.size();

            result = accMasterItemsDAOobj.getCreditNotesBySalesPerson(parameters); // Get Credit Note by Sales Person
            List list5 = result.getEntityList();
            int count5 = list5.size();
            if (count1 > 0 || count2 > 0 || count3 > 0 || count4 > 0 || count5 > 0) {
                throw new AccountingException("Master Item(s) are already used in transaction(s). ");
            }
        }
    }
    public void checkWCMasterItemUsedInAnyWC(HashMap<String, Object> parameters) throws AccountingException, ServiceException {
        if (parameters.containsKey("companyid") && parameters.get("companyid") != null) {
            KwlReturnObject result = accMasterItemsDAOobj.getWCByWCMasterItem(parameters); // Get Sales Order by Sales Person
            List list1 = result.getEntityList();
            int count1 = list1.size();
            if (count1 > 0) {
                throw new AccountingException("You cannot delete selected Master Item as it is already used in transaction(s).");
            }
        }
    }
    
    public void deleteMasterItemFromChildCompanies(JSONObject paramJobj, String childCompanyID, String childcompanysmasteritemrid) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false, isCommitEx = false;
        String msg = "";
        TransactionStatus status = null;
        Locale locale = null;

        try {
            String ids[] = new String[]{childcompanysmasteritemrid};
            String exceptMasterItems = "", usedInTransaction = "";
            int numRows = 0;

            Map<String, Object> auditRequestParams = new HashMap<String, Object>();
            auditRequestParams.put(Constants.reqHeader, paramJobj.getString(Constants.reqHeader));
            auditRequestParams.put(Constants.remoteIPAddress, paramJobj.getString(Constants.remoteIPAddress));
            auditRequestParams.put(Constants.useridKey, paramJobj.getString(Constants.useridKey));

            String[] group = new String[100];
            if (paramJobj.has("name") && paramJobj.get("name") != null) {
                String groupString = (String) paramJobj.get("name");
                group = groupString.split(",");
            }
            if (paramJobj.has(Constants.locale)) {
                locale = (Locale) paramJobj.get(Constants.locale);
            }
            String[] groupname = group;

            String masterGroup = paramJobj.optString("groupname", "");

//            String groupname[] = request.getParameterValues("name");
//            String masterGroup = request.getParameter("groupname");
            String companyId = childCompanyID;

            KwlReturnObject extracompanyprefObjresult = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), companyId);
            ExtraCompanyPreferences extracompanyobj = (ExtraCompanyPreferences) extracompanyprefObjresult.getEntityList().get(0);

            DefaultTransactionDefinition def = new DefaultTransactionDefinition();
            def.setName("MI_Tx");
            def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
            for (int i = 0; i < ids.length; i++) {
                String returnVal = "";
                try {
//                    status = txnManager.getTransaction(def);
                    KwlReturnObject result = accMasterItemsDAOobj.getSalesPersonMappedWithCustomer(ids[i]);
                    List list = result.getEntityList();
                    if (list != null && !list.isEmpty()) {
                        jobj.put("success", false);
                        jobj.put("msg", "Sorry, you cannot delete selected master item as it is already mapped with customer(s).");
//                    return new ModelAndView("jsonView", "model", jobj.toString());
                    }
                    if (masterGroup.equalsIgnoreCase("Paid To")) {
                        HashMap<String, Object> ibgBankDetailsMap = new HashMap<String, Object>();
                        ibgBankDetailsMap.put("companyid", companyId);
                        ibgBankDetailsMap.put("masterItemId", ids[i]);
                        accMasterItemsDAOobj.deleteIBGReceivingBankDetails(ibgBankDetailsMap);
                        accMasterItemsDAOobj.deleteCIMBReceivingBankDetails(ibgBankDetailsMap);
                    }
                    if (masterGroup.equalsIgnoreCase("Driver")) {
                        HashMap<String, Object> parameters = new HashMap<String, Object>();
                        parameters.put("companyid", companyId);
                        parameters.put("driverid", ids[i]);
                        parameters.put(Constants.locale, locale);
                        returnVal = groupname[i];
                        checkDriverUsedInAnyTransaction(parameters);
                    }
                    /*
                     * Checking whether Sales Person used in any transaction or
                     * not
                     */
                    if (masterGroup.equalsIgnoreCase("Sales Person")) {
                        HashMap<String, Object> parameters = new HashMap<String, Object>();
                        parameters.put("companyid", companyId);
                        parameters.put("masterItemId", ids[i]);
                        returnVal = groupname[i];
                        result = accMasterItemsDAOobj.checkSalesPersonUsedInAnyTransaction(parameters);
                    }

                    if (masterGroup.equalsIgnoreCase("Stock Adjustment Reason")) {
                        KwlReturnObject masterItemResult = accountingHandlerDAOobj.getObject(MasterItem.class.getName(), ids[i]);
                        MasterItem masterItem = (MasterItem) masterItemResult.getEntityList().get(0);
                        if (masterItem != null && masterItem.getDefaultMasterItem() != null && masterItem.getDefaultMasterItem().getID().equalsIgnoreCase(Constants.WASTAGE_ID)) {
                            returnVal = groupname[i];
                            throw new AccountingException("It is default master entry. So cannot deleted.");
                        }
                    }

                    if (masterGroup.equalsIgnoreCase("Sales Person")) {
                        HashMap<String, Object> parameters = new HashMap<String, Object>();
                        parameters.put("companyid", companyId);
                        parameters.put("salespersonid", ids[i]);
                        returnVal = groupname[i];
                        checkSalesPersonUsedInAnyTransaction(parameters);
                    } else if (masterGroup.equalsIgnoreCase("Agent")) {
                        HashMap<String, Object> parameters = new HashMap<String, Object>();
                        parameters.put("companyid", companyId);
                        parameters.put("agentid", ids[i]);
                        returnVal = groupname[i];
                        result = accMasterItemsDAOobj.checkAgentUsedInAnyTransaction(parameters); // Get Agent
                    }
                    returnVal = accMasterItemsDAOobj.daleteMasterItem(ids[i]);
                    numRows++;

                    auditTrailObj.insertAuditLog(AuditAction.MASTER_GROUP, "User " + paramJobj.optString(Constants.userfullname) + " has deleted master item " + returnVal + " for master group " + masterGroup, auditRequestParams, "0");

//                    txnManager.commit(status);
                } catch (Exception ex) {
                    isCommitEx = true;
                    exceptMasterItems += returnVal + ", ";
                    if (masterGroup.equalsIgnoreCase("Sales Person")) {
                        usedInTransaction = ex.getMessage();
                    }
                    if (status != null) {
//                        txnManager.rollback(status);
                    }
                }
            }
            issuccess = true;
            if (StringUtil.isNullOrEmpty(exceptMasterItems)) {
                msg = messageSource.getMessage("acc.master.del", null, StringUtil.getLocale(paramJobj.optString(Constants.language)));  //"Master item has been deleted successfully";
            } else {
                msg = usedInTransaction + messageSource.getMessage("acc.field.MasterItemsexcept", null, StringUtil.getLocale(paramJobj.optString(Constants.language))) + " <b>" + exceptMasterItems.substring(0, exceptMasterItems.length() - 2) + "</b> " + messageSource.getMessage("acc.field.hasbeendeletedsuccessfully", null, StringUtil.getLocale(paramJobj.optString(Constants.language)));
            }
        } catch (Exception ex) {
            if (status != null) {
//                txnManager.rollback(status);
            }
            msg = "" + ex.getMessage();
            Logger.getLogger(accMasterItemsController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accMasterItemsController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
  @Override  
    public JSONObject getMasterItemsForCustomFoHire(JSONObject paramJObj) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        try {
            HashMap<String, Object> filterRequestParams = new HashMap<String, Object>();
            ArrayList filter_names = new ArrayList(), filter_params = new ArrayList(), order_by = new ArrayList(), order_type = new ArrayList();
            filter_names.add("field.id");
            filter_params.add(paramJObj.optString("groupid"));
            order_by.add("value");
            order_type.add("asc");
            filterRequestParams.put("filter_names", filter_names);
            filterRequestParams.put("filter_params", filter_params);
            filterRequestParams.put("order_by", order_by);
            filterRequestParams.put("order_type", order_type);
            filterRequestParams.put("start", paramJObj.optString("start"));
            filterRequestParams.put("limit", paramJObj.optString("limit"));
            filterRequestParams.put("ss", paramJObj.optString("ss"));
            KwlReturnObject result = accMasterItemsDAOobj.getMasterItemsForCustomHire(filterRequestParams);

            List list = result.getEntityList();
            Iterator itr = list.iterator();
            JSONArray jArr = new JSONArray();
            String companyId = paramJObj.optString(Constants.companyKey);

            Map<String, Object> map = new HashMap();
            String masterFieldId = "";
            ExtraCompanyPreferences pref = (ExtraCompanyPreferences) kwlCommonTablesDAOObj.getClassObject(ExtraCompanyPreferences.class.getName(), companyId);
            if (pref != null) {
                if (pref.isUsersVisibilityFlow()) {
                    /**
                     * get Field id for masters to find users group tagged to it
                     */
                    String fieldlabel = paramJObj.optString("fieldlabel");
                    if (fieldlabel != null) {
                        int index = fieldlabel.lastIndexOf("*");
                        if (index != -1) {
                            fieldlabel = fieldlabel.substring(0, index);
                        }
                    }

                    map.put("fieldlabel", fieldlabel);
                    map.put("companyid", companyId);
                    KwlReturnObject kro = accMasterItemsDAOobj.getFieldIdForMaster(map);

                    if (kro.getEntityList().size() > 0 && kro.getEntityList().get(0) != null) {
                        masterFieldId = (String) kro.getEntityList().get(0);
                    }
                }
            }

            List ll = result.getEntityList();
            while (itr.hasNext()) {
                ArrayList filter_nameschield = new ArrayList(), filter_paramsChield = new ArrayList();
                HashMap<String, Object> filterRequestParamsChild = new HashMap<String, Object>();

                Object[] row = (Object[]) itr.next();
                FieldComboData fieldComboData = (FieldComboData) row[0];
                JSONObject obj = new JSONObject();
                obj.put("id", fieldComboData.getId());
                obj.put("name", fieldComboData.getValue());
                obj.put("itemdescription", StringUtil.isNullOrEmpty(fieldComboData.getItemdescription()) ? "" : fieldComboData.getItemdescription());
                if (fieldComboData.getSeqformat() != null) {
                    obj.put("sequenceformat", fieldComboData.getSeqformat().getID());
                } else {
                    obj.put("sequenceformat", "NA");
                }
                if (fieldComboData.isActivatedeactivatedimensionvalue()) {
                    obj.put("activatedeactivatedimension", "Activated");
                } else {
                    obj.put("activatedeactivatedimension", "Deactivated");
                }
                if (!StringUtil.isNullOrEmpty(fieldComboData.getSalesCommissionSchemaMaster())) {
                    KwlReturnObject schemaMaster = accMasterItemsDAOobj.getSalesCommissionSchema(fieldComboData.getSalesCommissionSchemaMaster(), companyId);
                    if (schemaMaster != null) {
                        obj.put("salesCommissionSchema", (String) schemaMaster.getEntityList().get(0));
                    }
                }
                FieldComboData parentItem = (FieldComboData) row[3];
                if (parentItem != null) {
                    obj.put("parentid", parentItem.getId());
                    obj.put("parentname", parentItem.getValue());
                }
                filter_nameschield.add("child.id");
                filter_paramsChield.add(fieldComboData.getId());
                filterRequestParamsChild.put("filter_names", filter_nameschield);
                filterRequestParamsChild.put("filter_params", filter_paramsChield);
                KwlReturnObject resultChildData = accMasterItemsDAOobj.getMasterItemsParentDimensionValue(filterRequestParamsChild);
                List lst = resultChildData.getEntityList();
                String parentidstr = getFieldParamsComboCommaSepValues(lst);
                obj.put("parentmappingid", parentidstr);
                String[] parentIds = parentidstr.split(",");
                String Values = "";
                for (int cnt = 0; cnt < parentIds.length; cnt++) {
                    KwlReturnObject rdresult = accountingHandlerDAOobj.getObject(FieldComboData.class.getName(), parentIds[cnt]);
                    FieldComboData ComboObj = (FieldComboData) rdresult.getEntityList().get(0);
                    if (ComboObj != null) {
                        Values += ComboObj.getValue() + ",";
                    }
                }
                if (!Values.isEmpty()) {
                    Values = Values.substring(0, Values.length() - 1);
                }
                obj.put("parentmappingname", Values);
                obj.put("level", row[1]);
                obj.put("leaf", row[2]);

                /**
                 * Put User group data Mapped to FCD
                 */
                if (pref.isUsersVisibilityFlow() && !StringUtil.isNullOrEmpty(masterFieldId)) {
                    map.clear();
                    map.put("masterFieldId", masterFieldId);
                    map.put("value", fieldComboData.getValue());
                    KwlReturnObject object = fieldManagerDAOobj.getUsersGroupMappedToFCD(map);
                    if (object.getEntityList().size() > 0) {
                        List<UserGroupFieldComboMapping> fieldComboMappings = object.getEntityList();
                        String usersGrp = "";
                        String usersGrpid = "";
                        for (UserGroupFieldComboMapping userGroupFieldComboMapping : fieldComboMappings) {
                            usersGrp += userGroupFieldComboMapping.getUsersGroup().getName() + ",";
                            usersGrpid += userGroupFieldComboMapping.getUsersGroup().getID() + ",";
                        }
                        if (!StringUtil.isNullOrEmpty(usersGrp)) {
                            usersGrp = usersGrp.substring(0, usersGrp.length() - 1);
                        }
                        if (!StringUtil.isNullOrEmpty(usersGrpid)) {
                            usersGrpid = usersGrpid.substring(0, usersGrpid.length() - 1);
                        }
                        obj.put("usergroup", usersGrp);
                        obj.put("usergroupid", usersGrpid);
                    }
                }
                jArr.put(obj);
            }
            jobj.put("data", jArr);
            jobj.put("totalCount", result.getRecordTotalCount());


        } catch (Exception e) {
            try {
                throw ServiceException.FAILURE(e.getMessage(), e);
            } catch (ServiceException ex) {
                Logger.getLogger(accMasterItemsController.class.getName()).log(Level.SEVERE, null, ex);
            }
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", "");
            } catch (JSONException ex) {
                Logger.getLogger(accMasterItemsController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return jobj;
    }
   
    public String getFieldParamsComboCommaSepValues(List list) throws JSONException, ServiceException, SessionExpiredException {
        Iterator itr = list.iterator();
        String parentidstr = "";
        while (itr.hasNext()) {
            FieldComboDataMapping comboDataMapping = (FieldComboDataMapping) itr.next();
            String id = comboDataMapping.getParent().getId();
            if (StringUtil.isNullOrEmpty(parentidstr)) {
                parentidstr = id;
            } else {
                parentidstr += "," + id;
            }
        }

        return parentidstr;
    }     
        
    @Override
    public JSONObject getMasterGroups(JSONObject paramJObj) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        try {
            jobj =getMasterGroupsJSON(paramJObj);
            issuccess = true;
        } catch (Exception ex) {
            msg = "accMasterItemsController.getMasterGroups : " + ex.getMessage();
            Logger.getLogger(accMasterItemsController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accMasterItemsController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return jobj;
    }
    
    public JSONObject getMasterGroupsJSON(JSONObject paramJObj) throws ServiceException, SessionExpiredException {
        JSONObject jobj = new JSONObject();
        String relatedModuleIds = "";
        int relatedModuleIsAllowEdit = Constants.ALLOW_TO_EDIT_PRODUCT_CUSTOMFIELD;
        try {
            KwlReturnObject result = accMasterItemsDAOobj.getMasterGroups();
            boolean isShowCustomColumn = false;
            boolean isShowDimensiononly = false;
            String companyid = paramJObj.optString(Constants.companyKey);
            boolean isShowCustomFieldonly = false;
            if (!StringUtil.isNullOrEmpty(paramJObj.optString("isShowCustColumn",null))) {
                isShowCustomColumn = Boolean.parseBoolean(paramJObj.optString("isShowCustColumn"));
            }
            if (!StringUtil.isNullOrEmpty(paramJObj.optString("isShowDimensiononly",null))) {
                isShowDimensiononly = Boolean.parseBoolean(paramJObj.optString("isShowDimensiononly"));
            }
            if (!StringUtil.isNullOrEmpty(paramJObj.optString("isShowCustomFieldonly",null))) {
                isShowCustomFieldonly = Boolean.parseBoolean(paramJObj.optString("isShowCustomFieldonly"));
            }
            if (isShowDimensiononly || isShowCustomFieldonly) {
                isShowCustomColumn = true;
            }
            List list = result.getEntityList();
            Iterator iter = list.iterator();
            JSONArray jArr = new JSONArray();
            KwlReturnObject cmp = accountingHandlerDAOobj.getObject(Company.class.getName(), companyid);
            Company company = (Company) cmp.getEntityList().get(0);
            KwlReturnObject extcmppref = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), companyid);
            ExtraCompanyPreferences extcmpprefObj = (ExtraCompanyPreferences) extcmppref.getEntityList().get(0);
            if (!isShowDimensiononly && !isShowCustomFieldonly) {
                while (iter.hasNext()) {
                    MasterGroup mst = (MasterGroup) iter.next();
                    JSONObject tmpObj = new JSONObject();
                    if (mst.isIsformrp() && !extcmpprefObj.isActivateMRPModule()) {
                    } else {
                        if (mst.getID().equals("59")) {
                            if (!company.getCountry().getID().equals("137")) {
                                continue;
                            }
                        }
                        tmpObj.put("id", mst.getID());
                        if (mst.getID().equals("37")) {
                            continue;
                        } else if (mst.getID().equals("52") || mst.getID().equals("47")) {
                            /**
                             * ERP-35347
                             */
                            continue;
                        }
                        tmpObj.put("iscustomfield", "-");
                        tmpObj.put("name", messageSource.getMessage("acc.masterConfig." + mst.getID(), null, Locale.forLanguageTag(paramJObj.getString("language"))));
                        jArr.put(tmpObj);
                    }
                }
            }
            if (isShowCustomColumn) {
                HashMap<String, Object> requestParams = new HashMap<String, Object>();
                requestParams.put("filter_names", Arrays.asList("companyid"));
                requestParams.put("filter_values", Arrays.asList(companyid));

                if (isShowDimensiononly) {
                    requestParams = new HashMap<String, Object>();
                    requestParams.put("filter_names", Arrays.asList("companyid", "customfield"));
                    requestParams.put("filter_values", Arrays.asList(companyid, 0));
                }
                if (isShowCustomFieldonly) {
                    requestParams = new HashMap<String, Object>();
                    requestParams.put("filter_names", Arrays.asList("companyid", "customfield"));
                    requestParams.put("filter_values", Arrays.asList(companyid, 1));
                }
                result = accMasterItemsDAOobj.getFieldParamsUsingSql(requestParams);

                list = result.getEntityList();
                iter = list.iterator();

                while (iter.hasNext()) {
                    Object[] temp = (Object[]) iter.next();
                    FieldParams fieldParams = (FieldParams) kwlCommonTablesDAOObj.getClassObject(FieldParams.class.getName(), temp[1].toString());
                    KwlReturnObject resultModuleids = accMasterItemsDAOobj.getallModuleNamesUsingSql(temp[2].toString());
                    List listName = resultModuleids.getEntityList();
                    Iterator iterator = listName.iterator();
                    String allNameFromId = "";
                    String activeModuleNames = "";
                    int i = 0;
                    while (iterator.hasNext()) {
                        String coma = "";
                        if (i != 0) {
                            coma = ", ";
                        }
                        Object[] temp1 = (Object[]) iterator.next();
                        String moduleName = getModuleName(Integer.parseInt(temp1[0].toString()));
                        allNameFromId = allNameFromId + coma + moduleName;
                        i++;
                        if (String.valueOf(temp1[1]) != "null") {
                            relatedModuleIds = String.valueOf(temp1[1]);
                        }
                        /**
                         * Getting the value of relatedModuleIsAllowEdit from
                         * database and passing it to jSON only for Product
                         * Master Module ERM-177 / ERP-34804.
                         */
                        if (!StringUtil.isNullObject(temp1[3]) && !StringUtil.isNullObject(temp1[0]) && ((Short) temp1[0] == Constants.Acc_Product_Master_ModuleId)) {
                            relatedModuleIsAllowEdit = (Integer) temp1[3];
                        }

                        int activated = Integer.parseInt(temp1[2].toString());
                        if (activated == 1) {
                            activeModuleNames += activeModuleNames.equals("") ? moduleName : ("," + moduleName);
                        }
                    }
                    String fieldName = fieldParams.getFieldlabel();
                    String fieldNameAppendString = (fieldParams.getIsessential() == 1) ? "*" : "";
                    JSONObject tmpObj = new JSONObject();
                    tmpObj.put("id", fieldParams.getId());
                    tmpObj.put("name", fieldName + fieldNameAppendString);
                    tmpObj.put("modulename", getModuleName(fieldParams.getModuleid()));
                    tmpObj.put("fieldtype", fieldParams.getFieldtype());
                    tmpObj.put("moduleIds", temp[2].toString());
                    if (fieldParams.getParent() != null) {
                        tmpObj.put("itemparentid", fieldParams.getParent().getId());
                    }
                    tmpObj.put("iscustomfield", fieldParams.getCustomfield() == 1 ? true : false);
                    tmpObj.put("isfortask", fieldParams.getisfortask() == 1 ? true : false);
                    tmpObj.put("customcolumn", fieldParams.getCustomcolumn());
                    tmpObj.put("allmodulenames", allNameFromId);
                    tmpObj.put("activemodulenames", activeModuleNames);
                    tmpObj.put("relatedModuleIds", relatedModuleIds);
                    tmpObj.put("relatedModuleIsAllowEdit", relatedModuleIsAllowEdit);       //ERM-177 / ERP-34804
                    tmpObj.put("mapwithtype", fieldParams.getmapwithtype());
                    tmpObj.put("isforproject", fieldParams.getisforproject() == 1 ? true : false);
                    tmpObj.put("isforeclaim", fieldParams.getIsforeclaim() == 1 ? true : false);
                    tmpObj.put(Constants.isformultientity, fieldParams.isFieldOfGivenGSTConfigType(Constants.isformultientity));
                    tmpObj.put(Constants.GST_CONFIG_TYPE, fieldParams.getGSTConfigType());
                    tmpObj.put(Constants.ISFORSALESCOMMISSION, fieldParams.isIsForSalesCommission());
                    tmpObj.put(Constants.isForKnockOff, fieldParams.isIsForKnockOff());
                    tmpObj.put("isessential", fieldParams.getIsessential());
                    relatedModuleIds = "";
                    relatedModuleIsAllowEdit = Constants.ALLOW_TO_EDIT_PRODUCT_CUSTOMFIELD;
                    allNameFromId = "";
                    jArr.put(tmpObj);
                }
            }
            jobj.put("data", jArr);
        } catch (JSONException e) {
            throw ServiceException.FAILURE(e.getMessage(), e);
        }
        return jobj;
    } 
   
  @Override  
    public String getModuleName(int moduleid) {
        String moduleName = "";
        switch (moduleid) {
            case (Constants.Acc_Invoice_ModuleId):
                moduleName = "Invoice/Cash Sales";
                break;
            case (Constants.Acc_BillingInvoice_ModuleId):
                moduleName = "Billing Invoice";
                break;
            case (Constants.Acc_Cash_Sales_ModuleId):
                moduleName = "Cash Sales";
                break;
            case (Constants.Acc_Billing_Cash_Sales_ModuleId):
                moduleName = "Billing Cash Sales";
                break;
            case (Constants.Acc_Vendor_Invoice_ModuleId):
                moduleName = "Purchase Invoice/Cash Purchase";
                break;
            case (Constants.Acc_Debit_Note_ModuleId):
                moduleName = "Debit Note";
                break;
            case (Constants.Acc_Credit_Note_ModuleId):
                moduleName = "Credit Note";
                break;
            case (Constants.Acc_Make_Payment_ModuleId):
                moduleName = "Make Payment";
                break;
            case (Constants.Acc_Receive_Payment_ModuleId):
                moduleName = "Receive Payment";
                break;
            case (Constants.Acc_GENERAL_LEDGER_ModuleId):
                moduleName = "Journal Entry";
                break;
            case (Constants.Acc_Product_Master_ModuleId):
                moduleName = "Products & Services";
                break;
            case (Constants.Acc_Purchase_Order_ModuleId):
                moduleName = "Purchase Order";
                break;
            case (Constants.Acc_Sales_Order_ModuleId):
                moduleName = "Sales Order";
                break;
            case (Constants.Acc_Customer_Quotation_ModuleId):
                moduleName = "Customer Quotation";
                break;
            case (Constants.Acc_Vendor_Quotation_ModuleId):
                moduleName = "Vendor Quotation";
                break;
            case (Constants.Acc_Delivery_Order_ModuleId):
                moduleName = "Delivery Order";
                break;
            case (Constants.Acc_Goods_Receipt_ModuleId):
                moduleName = "Goods Receipt Order";
                break;
            case (Constants.Acc_Sales_Return_ModuleId):
                moduleName = "Sales Return";
                break;
            case (Constants.Acc_Purchase_Return_ModuleId):
                moduleName = "Purchase Return";
                break;
            case (Constants.Account_Statement_ModuleId):
                moduleName = "GL Accounts";
                break;
            case (Constants.Acc_Vendor_ModuleId):
                moduleName = "Vendor";
                break;
            case (Constants.Acc_Customer_ModuleId):
                moduleName = "Customer";
                break;
            case (Constants.Acc_Purchase_Requisition_ModuleId):
                moduleName = "Purchase Requisition";
                break;
            case (Constants.Acc_Lease_Order_ModuleId):
                moduleName = "Lease Order";
                break;
            case (Constants.Acc_Contract_Order_ModuleId):
                moduleName = "Contract Order";
                break;
            case (Constants.Acc_RFQ_ModuleId):
                moduleName = "Request For Quotation";
                break;
            case (Constants.Acc_FixedAssets_DisposalInvoice_ModuleId):
                moduleName = "FA Disposal Invoice";
                break;
            case (Constants.Acc_FixedAssets_PurchaseInvoice_ModuleId):
                moduleName = "FA Purchase Invoice";
                break;
            case (Constants.Acc_FixedAssets_GoodsReceipt_ModuleId):
                moduleName = "FA Goods Receipt";
                break;
            case (Constants.Acc_FixedAssets_DeliveryOrder_ModuleId):
                moduleName = "FA Delivery Order";
                break;
            case (Constants.Acc_FixedAssets_AssetsGroups_ModuleId):
                moduleName = "FA Assets Group";
                break;
            case (Constants.Acc_FixedAssets_PurchaseRequisition_ModuleId):
                moduleName = "FA Purchase Requisition";
                break;
            case (Constants.Acc_FixedAssets_RFQ_ModuleId):
                moduleName = "FA RFQ";
                break;
            case (Constants.Acc_FixedAssets_Vendor_Quotation_ModuleId):
                moduleName = "FA Vendor Quotation";
                break;
            case (Constants.Acc_FixedAssets_Purchase_Order_ModuleId):
                moduleName = "FA Purchase Order";
                break;
            case (Constants.SerialWindow_ModuleId):
                moduleName = "Serial Window";
                break;
            case (Constants.Acc_ConsignmentRequest_ModuleId):
                moduleName = "Consignment Request";
                break;
            case (Constants.Acc_ConsignmentDeliveryOrder_ModuleId):
                moduleName = "Consignment DeliveryOrder";
                break;
            case (Constants.Acc_ConsignmentInvoice_ModuleId):
                moduleName = "Consignment Invoice";
                break;
            case (Constants.Acc_ConsignmentSalesReturn_ModuleId):
                moduleName = "Consignment SalesReturn";
                break;
            case (Constants.Acc_Consignment_GoodsReceiptOrder_ModuleId):
                moduleName = "Consignment GoodsReceiptOrder";
                break;
            case (Constants.Acc_Consignment_GoodsReceipt_ModuleId):
                moduleName = "Consignment GoodsReceipt";
                break;
            case (Constants.Acc_ConsignmentPurchaseReturn_ModuleId):
                moduleName = "Consignment PurchaseReturn";
                break;
            case (Constants.Acc_ConsignmentVendorRequest_ModuleId):
                moduleName = "Consignment VendorRequest";
                break;
            case (Constants.Inventory_ModuleId):
                moduleName = "Inventory Window";
                break;
            case (Constants.LEASE_INVOICE_MODULEID):
                moduleName = "Lease Invoice";
                break;
            case (Constants.Acc_Lease_Contract):
                moduleName = "Lease Contract";
                break;
            case (Constants.Acc_Lease_Quotation):
                moduleName = "Lease Quotation";
                break;
            case (Constants.Acc_Lease_DO):
                moduleName = "Lease Delivery Order";
                break;
            case (Constants.Acc_Lease_Return):
                moduleName = "Lease Return";
                break;
            case (Constants.Acc_FixedAssets_Purchase_Return_ModuleId):
                moduleName = "FA Purchase Return";
                break;
            case (Constants.Acc_FixedAssets_Sales_Return_ModuleId):
                moduleName = "FA Sales Return";
                break;
            case (Constants.Acc_FixedAssets_Details_ModuleId):
                moduleName = "FA Details";
                break;
            case (Constants.Inventory_Stock_Adjustment_ModuleId):
                moduleName = "Stock Adjustment";
                break;
            case (Constants.Acc_Stock_Request_ModuleId):
                moduleName = "Stock Request";
                break;
            case (Constants.Acc_InterStore_ModuleId):
                moduleName = "Inter Store Transfer";
                break;
            case (Constants.Acc_InterLocation_ModuleId):
                moduleName = "Inter Location Transfer";
                break;
            case (Constants.Labour_Master):
                moduleName = "Labour";
                break;
            case (Constants.MRP_WORK_CENTRE_MODULEID):
                moduleName = "Work Center Master";
                break;
            case (Constants.MRP_Machine_Management_ModuleId):
                moduleName = "Machine Master";
                break;
            case (Constants.MRP_WORK_ORDER_MODULEID):
                moduleName = "Work Order";
                break;
            case (Constants.VENDOR_JOB_WORKORDER_MODULEID):
                moduleName = "Vendor Job Work Order";
                break;
            case (Constants.MRP_Contract):
                moduleName = "Master Contract";
                break;
            case (Constants.MRP_RouteCode):
                moduleName = "Routing Template";
                break;
            case (Constants.MRP_JOB_WORK_MODULEID):
                moduleName = "Job Work";
                break;
            case (Constants.Acc_CycleCount_ModuleId):
                moduleName = "Cycle Count";
                break;
            case (Constants.Acc_SecurityGateEntry_ModuleId):
                moduleName = "Security Gate Entry";
                break;
            case (Constants.Acc_Multi_Entity_Dimension_MODULEID):
                moduleName = Constants.Title_MultiEntityDimension;
                break;
            case (Constants.JOB_WORK_OUT_ORDER_MODULEID):
                moduleName = Constants.JOBWORK_OUT_FLOW;
                break;

        }
        return moduleName;
    }
                        }
