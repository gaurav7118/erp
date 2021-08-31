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
package com.krawler.spring.accounting.masteritems;

import com.krawler.common.admin.*;
import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.common.util.Constants;
import com.krawler.common.util.StringUtil;
import com.krawler.esp.handlers.StorageHandler;
import com.krawler.hql.accounting.AccountingException;
import com.krawler.hql.accounting.MasterGroup;
import com.krawler.hql.accounting.MasterItem;
import com.krawler.spring.accounting.handler.AccountingHandlerDAO;
import com.krawler.spring.accounting.handler.AccountingManager;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.spring.common.kwlCommonTablesController;
import com.krawler.spring.common.kwlCommonTablesDAO;
import com.krawler.spring.sessionHandler.sessionHandlerImpl;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import com.krawler.spring.exportFunctionality.exportMPXDAOImpl;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.hibernate.Session;

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
import com.krawler.esp.handlers.APICallHandlerService;
import com.krawler.hql.accounting.*;
import com.krawler.hql.accounting.masteritems.service.AccMasterItemsService;
import com.krawler.inventory.model.location.Location;
import com.krawler.inventory.model.store.Store;
import com.krawler.inventory.model.store.StoreDAO;
import com.krawler.inventory.model.store.StoreType;
import com.krawler.spring.auditTrailModule.auditTrailDAO;
import com.krawler.spring.authHandler.authHandler;
import com.krawler.spring.common.AccCommonTablesDAO;
import com.krawler.spring.common.fieldManagerDAO;
import com.krawler.spring.companyDetails.companyDetailsDAO;
import com.krawler.spring.importFunctionality.ImportDAO;
import com.krawler.spring.importFunctionality.ImportHandler;
import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import com.krawler.inventory.model.store.StoreService;
/**
 *
 * @author krawler
 */
public class accMasterItemsController extends MultiActionController implements MessageSourceAware {

    private HibernateTransactionManager txnManager;
    private accMasterItemsDAO accMasterItemsDAOobj;
    private String successView;
    private MessageSource messageSource;
    private kwlCommonTablesDAO kwlCommonTablesDAOObj;
    private AccountingHandlerDAO accountingHandlerDAOobj;
    private auditTrailDAO auditTrailObj;
    private exportMPXDAOImpl exportDaoObj;
    public ImportHandler importHandler;
    private ImportDAO importDao;
    private AccCommonTablesDAO accCommonTablesDAO;
    private APICallHandlerService apiCallHandlerService;        //VP
    private companyDetailsDAO companyDetailsDAOObj;
    private AccMasterItemsService accMasterItemsService;
    private String auditMsg="",auditID="",action ="";
    private fieldManagerDAO fieldManagerDAOobj;
    private StoreDAO storeDAO;
    private StoreService storeService;
    
    @Override
    public void setMessageSource(MessageSource ms) {
        this.messageSource = ms;
    }

    public void setTxnManager(HibernateTransactionManager txManager) {
        this.txnManager = txManager;
    }

    public void setaccMasterItemsDAO(accMasterItemsDAO accMasterItemsDAOobj) {
        this.accMasterItemsDAOobj = accMasterItemsDAOobj;
    }

    public String getSuccessView() {
        return successView;
    }

    public void setSuccessView(String successView) {
        this.successView = successView;
    }

    public void setStoreDAO(StoreDAO storeDAO) {
        this.storeDAO = storeDAO;
    }

    public void setkwlCommonTablesDAO(kwlCommonTablesDAO kwlCommonTablesDAOObj1) {
        this.kwlCommonTablesDAOObj = kwlCommonTablesDAOObj1;
    }

    public void setaccountingHandlerDAO(AccountingHandlerDAO AccountingHandlerDAOobj) {
        this.accountingHandlerDAOobj = AccountingHandlerDAOobj;
    }

    public void setauditTrailDAO(auditTrailDAO auditTrailDAOObj) {
        this.auditTrailObj = auditTrailDAOObj;
    }

    public void setexportMPXDAOImpl(exportMPXDAOImpl exportDaoObj) {
        this.exportDaoObj = exportDaoObj;
    }

    public void setimportHandler(ImportHandler importHandler) {
        this.importHandler = importHandler;
    }

    public void setimportDAO(ImportDAO importDao) {
        this.importDao = importDao;
    }
    
    public void setaccCommonTablesDAO(AccCommonTablesDAO accCommonTablesDAO) {
        this.accCommonTablesDAO = accCommonTablesDAO;
    }

    public void setApiCallHandlerService(APICallHandlerService apiCallHandlerService) {     //VP
        this.apiCallHandlerService = apiCallHandlerService;
    }
    
      public void setcompanyDetailsDAO(companyDetailsDAO companyDetailsDAOObj1) {
        this.companyDetailsDAOObj = companyDetailsDAOObj1;
    }
       public void setAccMasterItemsService(AccMasterItemsService accMasterItemsService) {
        this.accMasterItemsService = accMasterItemsService;
    } 
    public void setFieldManagerDAO(fieldManagerDAO fieldManagerDAOobj) {
        this.fieldManagerDAOobj = fieldManagerDAOobj;
    }

    public ModelAndView saveMasterItem(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("MI_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        MasterItem masterItem=null;
        boolean isEdit=false;
        try {
            KwlReturnObject result = saveMasterItem(request);
            issuccess = true;
            msg = result.getMsg();
            if (result.getEntityList() != null) {
                 masterItem = (MasterItem) result.getEntityList().get(0);
                jobj.put("id", masterItem.getID());
                String name = request.getParameter("name").trim();
                String masterGroup = request.getParameter("groupname");
                isEdit = StringUtil.isNullOrEmpty(request.getParameter("isEdit")) ? false : Boolean.parseBoolean(request.getParameter("isEdit"));
                String action = "added";
                if (isEdit == true) {
                    action = "updated";
                }
                auditTrailObj.insertAuditLog(AuditAction.MASTER_GROUP, "User " + sessionHandlerImpl.getUserFullName(request) + " has " + action + " master item " + name + " for master group " + masterGroup, request, masterItem.getID());
                /*
                Sync Proccess and Skills or Department to PM 
                */
                KwlReturnObject extracompanyprefObjresult = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), sessionHandlerImpl.getCompanyid(request));
                ExtraCompanyPreferences extracompanyobj = (ExtraCompanyPreferences) extracompanyprefObjresult.getEntityList().get(0);
                if (extracompanyobj != null && extracompanyobj.isActivateMRPModule()) {
                    if (masterItem.getMasterGroup().getID().equalsIgnoreCase(masterItem.Process) || masterItem.getMasterGroup().getID().equalsIgnoreCase(MasterItem.Skill) || masterItem.getMasterGroup().getID().equalsIgnoreCase(masterItem.Department)) {
                        Map<String, Object> requestParams = new HashMap<String, Object>();
                        requestParams.put("companyId", sessionHandlerImpl.getCompanyid(request));
                        requestParams.put("masterItem", masterItem);
                        requestParams.put("userId", sessionHandlerImpl.getUserid(request));
                        accMasterItemsService.sendMasterToPM(requestParams);
                    }

                }
            }
            txnManager.commit(status);
            
              //*****************************************Propagate masteritem In child companies**************************
            String auditID = "";
            boolean propagateTOChildCompaniesFalg = false;
            String childCompanyName = "";
            
            if (!StringUtil.isNullOrEmpty(request.getParameter("ispropagatetochildcompanyflag"))) {
                propagateTOChildCompaniesFalg = Boolean.parseBoolean(request.getParameter("ispropagatetochildcompanyflag"));
            }
            if (propagateTOChildCompaniesFalg) {
            
                try {
                    String parentcompanyid = sessionHandlerImpl.getCompanyid(request);
                    Map<String, Object> parentdataMap =(Map<String, Object>) result.getEntityList().get(1);
                    if (!isEdit) {
                       addPropagatedMasterItemInEditCase(request, def, isEdit, parentdataMap, parentcompanyid, masterItem.getID(), masterItem.getValue());
                    }else{
                    auditID = AuditAction.MASTER_GROUP;
                        
                        HashMap<String, Object> filterRequestParams = new HashMap<String, Object>();
                        ArrayList filter_names = new ArrayList(), filter_params = new ArrayList();
                        filter_names.add("propagatedMasteritemID.ID");
                        filter_params.add(masterItem.getID());
                       
                        filterRequestParams.put("filter_names", filter_names);
                        filterRequestParams.put("filter_params", filter_params);
                        KwlReturnObject cntResult = accMasterItemsDAOobj.getMasterItems(filterRequestParams);
                        List childCompaniesMasterItemList = cntResult.getEntityList();
                        
                        if (childCompaniesMasterItemList.size() < 1) {
                            addPropagatedMasterItemInEditCase(request, def, isEdit, parentdataMap, parentcompanyid, masterItem.getID(), masterItem.getValue());
                        } else {
                            for (Object childObj : childCompaniesMasterItemList) {
                                try {
                                    MasterItem childmasteritem = (MasterItem) childObj;
                                    if (childmasteritem != null) {
                                        status = txnManager.getTransaction(def);
                                        String childcompanysmasteritemrid = childmasteritem.getID();
                                        String childCompanyID = childmasteritem.getCompany().getCompanyID();
                                        childCompanyName = childmasteritem.getCompany().getSubDomain();
                                        parentdataMap.put("childmasteritemid", childmasteritem.getID());
                                        savemasterItemInChildCompanies(isEdit, parentdataMap, childCompanyID, parentcompanyid, masterItem.getID());
//                                    
                                        txnManager.commit(status);
                                        status = null;
                                        auditTrailObj.insertAuditLog(auditID, "User " + sessionHandlerImpl.getUserFullName(request) + " has propagated(updated) masteritem" + masterItem.getValue() + " to child company " + childCompanyName, request, masterItem.getID());
                                    }
                                } catch (Exception ex) {
                                    txnManager.rollback(status);
                                    auditTrailObj.insertAuditLog(auditID, "User " + sessionHandlerImpl.getUserFullName(request) + " failed to  propagate(update) masteritem" + masterItem.getValue() + " to child company " + childCompanyName, request, masterItem.getID());
                                }
                            }
                        }
                    }
                } catch (Exception ex) {
                    if (status != null) {
                        txnManager.rollback(status);
                    auditTrailObj.insertAuditLog(auditID, "User " + sessionHandlerImpl.getUserFullName(request) + " failed to  propagate masteritem" + masterItem.getValue()+ " to child company " + childCompanyName, request, masterItem.getID());
                    }
                }
            }
             //*****************************************Propagate masteritem In child companies Ends Here**************************

        } catch (Exception ex) {
            txnManager.rollback(status);
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
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    
    public void addPropagatedMasterItemInEditCase(HttpServletRequest request, DefaultTransactionDefinition def, boolean isEdit, Map<String, Object> parentdataMap, String parentcompanyid, String parentmasteritemid, String masterItemName) throws ServiceException {
        TransactionStatus status = null;
        try {
            String childCompanyName = "";
            isEdit=false;// control comes in this method only when master item is completely new for child company.so isEdit is kept false
            List childCompaniesList = companyDetailsDAOObj.getChildCompanies(parentcompanyid);
            auditID = AuditAction.MASTER_GROUP;
            for (Object childObj : childCompaniesList) {
                try {
                    status = txnManager.getTransaction(def);
                    Object[] childdataOBj = (Object[]) childObj;
                    String childCompanyID = (String) childdataOBj[0];
                    childCompanyName = (String) childdataOBj[1];
                    savemasterItemInChildCompanies(isEdit, parentdataMap, childCompanyID, parentcompanyid, parentmasteritemid);
                    txnManager.commit(status);
                    status = null;
                    auditTrailObj.insertAuditLog(auditID, "User " + sessionHandlerImpl.getUserFullName(request) + " has propagated(added) masteritem" + masterItemName + " to child company " + childCompanyName, request, parentmasteritemid);
                } catch (Exception ex) {
                    txnManager.rollback(status);
                    auditTrailObj.insertAuditLog(auditID, "User " + sessionHandlerImpl.getUserFullName(request) + " failed to  propagate(add) masteritem" + masterItemName + " to child company " + childCompanyName, request, parentmasteritemid);
                    throw ServiceException.FAILURE("accMasterItemsController.addPropagatedMasterItemInEditCase", ex);
                }
            }
        } catch (Exception ex) {
            if (status != null) {
                txnManager.rollback(status);
            }
            Logger.getLogger(accMasterItemsController.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE("accMasterItemsController.addPropagatedMasterItemInEditCase", ex);
        }
    }
    public void savemasterItemInChildCompanies(boolean isEdit,Map<String, Object> parentdataMap,String childcompnayid,String parentcompanyid,String parentCompanysMasterItemId) throws ServiceException{
        
        String itemID="";
        KwlReturnObject result=null;
        parentdataMap.remove("id");
        HashMap<String,Object> dataMap=(HashMap<String,Object> )parentdataMap;
        if (!isEdit) {
            HashMap<String, Object> filterRequestParams = new HashMap<String, Object>();
            ArrayList filter_names = new ArrayList(), filter_params = new ArrayList();
            filter_names.add("masterGroup.ID");
            filter_params.add(parentdataMap.get("groupid"));
            filter_names.add("company.companyID");
            filter_params.add(childcompnayid);
            filter_names.add("value");
            filter_params.add(parentdataMap.get("name"));
            filterRequestParams.put("filter_names", filter_names);
            filterRequestParams.put("filter_params", filter_params);
            KwlReturnObject cntResult = accMasterItemsDAOobj.getMasterItems(filterRequestParams);
            int count = cntResult.getRecordTotalCount();

            if (count >= 1) {
                return;
            }
        }else{
         dataMap.put("id",parentdataMap.get("childmasteritemid"));
        }
        dataMap.put("companyid",childcompnayid);
        dataMap.put("parentCompanysMasterItemId",parentCompanysMasterItemId);
        
       
        if (parentdataMap.containsKey("parentid") && !StringUtil.isNullOrEmpty((String) parentdataMap.get("parentid"))) {
            getmasteritemByname(childcompnayid, parentcompanyid, parentdataMap, dataMap, "parentid",(String)parentdataMap.get("groupid"));
        }
        if (parentdataMap.containsKey("driverID") && parentdataMap.get("driverID") != null) {
            getmasteritemByname(childcompnayid, parentcompanyid, parentdataMap, dataMap, "driverID",(String)parentdataMap.get("groupid"));
        }
        
        try{
              if (!isEdit) {
                    result = accMasterItemsDAOobj.addMasterItem(dataMap);
                } else {
                    result = accMasterItemsDAOobj.updateMasterItem(dataMap);
                }
        }catch(Exception ex){
        throw ServiceException.FAILURE("accMasterItemsController.savemasterItemInChildCompanies", ex);
        }
    }
    
    public void getmasteritemByname(String childcompnayid,String parentcompanyid,Map<String, Object> parentdataMap,Map<String, Object> dataMap ,String key,String groupid) {
        String data = "";
        String fetchColumn = "mst.value";
        String conditionColumn = "mst.ID";
        try {
            String masterGroupID = groupid;
            KwlReturnObject returnObject = accMasterItemsDAOobj.getMasterItemByNameorID(parentcompanyid, (String) parentdataMap.get(key), masterGroupID, fetchColumn, conditionColumn);
            data = (String) returnObject.getEntityList().get(0);

            returnObject = accMasterItemsDAOobj.getMasterItemByNameorID(childcompnayid, data, masterGroupID, conditionColumn, fetchColumn);
            data = (String) returnObject.getEntityList().get(0);
            dataMap.put(key, data);
        } catch (Exception ex) {
               Logger.getLogger(accMasterItemsController.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
    /**
     * method use to save activate and deactivate staus for dimensio field values
     */
    public ModelAndView saveActivateDeactivateDimensionFields(HttpServletRequest request,HttpServletResponse response){
        JSONObject jobj = new JSONObject();
         boolean issuccess = false;
        String msg = "";
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("MI_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        String data = StringUtil.isNullObject(request.getParameter("data"))?"":request.getParameter("data");
        boolean activateDeactivateDimFlag = StringUtil.isNullObject(request.getParameter("activateDeactivateDimFlag"))? false :Boolean.parseBoolean(request.getParameter("activateDeactivateDimFlag"));
        try {
            HashMap<String,Object> requestparams = new HashMap<>();
            requestparams.put("activateDeactivateDimFlag", activateDeactivateDimFlag);
            requestparams.put("data", data);
            KwlReturnObject result = accMasterItemsDAOobj.saveActivateDeactivateDimensionFields(requestparams);
            issuccess = true;
            if(activateDeactivateDimFlag){
                msg=messageSource.getMessage("acc.masterconfiguration.DimensionFieldActivatedsuccessfully", null, RequestContextUtils.getLocale(request));
            }else{
                msg=messageSource.getMessage("acc.masterconfiguration.DimensionFieldDeactivatedsuccessfully", null, RequestContextUtils.getLocale(request));
            }
            txnManager.commit(status);
        } catch (Exception ex) {
            txnManager.rollback(status);
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
        return new ModelAndView("jsonView","model",jobj.toString());
    }
    public ModelAndView saveMasterItemPrice(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("MI_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        try {
            KwlReturnObject result = saveMasterItemPrice(request);
            issuccess = true;
            msg = result.getMsg();
            if (result.getEntityList() != null) {
                MasterItemPrice masterItemPrice = (MasterItemPrice) result.getEntityList().get(0);
                jobj.put("id", masterItemPrice.getID());
                
            }
            txnManager.commit(status);

        } catch (Exception ex) {
            txnManager.rollback(status);
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
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    
    public ModelAndView saveMasterItemPriceFormula(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("MI_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        try {
            KwlReturnObject result = saveMasterItemPriceFormula(request);
            issuccess = true;
            msg = result.getMsg();
            if (result.getEntityList() != null) {
                MasterItemPriceFormula masterItemPriceFormula = (MasterItemPriceFormula) result.getEntityList().get(0);
                jobj.put("id", masterItemPriceFormula.getID());
                
            }
            txnManager.commit(status);

        } catch (Exception ex) {
            txnManager.rollback(status);
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
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public ModelAndView saveLocationItem(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("MI_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        try {
            KwlReturnObject result = saveLocationItem(request);
            issuccess = true;
            msg = result.getMsg();
            if (result.getEntityList() != null) {
                InventoryLocation inventoryLocation = (InventoryLocation) result.getEntityList().get(0);
                jobj.put("id", inventoryLocation.getId());
                String name = request.getParameter("name");
                String groupName = request.getParameter("groupName");
                boolean isEdit = StringUtil.isNullOrEmpty(request.getParameter("isEdit")) ? false : Boolean.parseBoolean(request.getParameter("isEdit"));
                String action = "added";
                if (isEdit) {
                    action = "updated";
                }
                auditTrailObj.insertAuditLog(AuditAction.LOCATION_CHANGED, "User " + sessionHandlerImpl.getUserFullName(request) + " has " + action + " location item " + name, request, "0");
                

            }
            txnManager.commit(status);

        } catch (Exception ex) {
            txnManager.rollback(status);
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
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public ModelAndView saveDepartmentItem(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("MI_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        try {
            KwlReturnObject result = saveDepartmentItem(request);
            issuccess = true;
            msg = result.getMsg();
            if (result.getEntityList() != null) {
                Department department = (Department) result.getEntityList().get(0);
                jobj.put("id", department.getId());
                String name = request.getParameter("name");
                String groupName = request.getParameter("groupName");
                boolean isEdit = StringUtil.isNullOrEmpty(request.getParameter("isEdit")) ? false : Boolean.parseBoolean(request.getParameter("isEdit"));
                String action = "added";
                String auditaction=AuditAction.DEPARTMENT_ADDED;
                if (isEdit) {
                    action = "updated";
                    auditaction=AuditAction.DEPARTMENT_UPDATED;
                }
                auditTrailObj.insertAuditLog(auditaction, "User " + sessionHandlerImpl.getUserFullName(request) + " has " + action + " department item " + name, request, "0");
                
            }
            txnManager.commit(status);

        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = "" + ex.getMessage();
            Logger.getLogger(accMasterItemsController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accMasterItemsController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public ModelAndView saveWarehouseItem(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("MI_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        try {
            KwlReturnObject result = saveWarehouseItem(request);
            issuccess = true;
            msg = result.getMsg();
            if (result.getEntityList() != null) {
                InventoryWarehouse inventoryWarehouse = (InventoryWarehouse) result.getEntityList().get(0);
                jobj.put("id", inventoryWarehouse.getId());
                String name = request.getParameter("name");
                String groupName = request.getParameter("groupName");
                boolean isEdit = StringUtil.isNullOrEmpty(request.getParameter("isEdit")) ? false : Boolean.parseBoolean(request.getParameter("isEdit"));
                String action = "added";
                if (isEdit) {
                    action = "updated";
                }
                auditTrailObj.insertAuditLog(AuditAction.WAREHOUSE_CHANGED, "User " + sessionHandlerImpl.getUserFullName(request) + " has " + action + " warehouse item " + name, request, "0");
                
            }
            txnManager.commit(status);

        } catch (Exception ex) {
            txnManager.rollback(status);
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
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public ModelAndView saveMasterItemForCustom(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        boolean isEdit=false;
        FieldComboData fieldComboData=null;
        String auditaction="";
        String msg = "";
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("MI_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        try {
            KwlReturnObject result = saveMasterItemForCustom(request);
            if (result.isSuccessFlag()) {
                issuccess = true;
                msg = result.getMsg();
                 fieldComboData = (FieldComboData) result.getEntityList().get(0);
                jobj.put("id", fieldComboData.getId());
                String name = request.getParameter("name");
                String masterGroup = request.getParameter("groupname");
                String customcolumn = request.getParameter("customcolumn");
                 isEdit = StringUtil.isNullOrEmpty(request.getParameter("isEdit")) ? false : Boolean.parseBoolean(request.getParameter("isEdit"));
                boolean iscustom = StringUtil.isNullOrEmpty(request.getParameter("iscustom")) ? false : Boolean.parseBoolean(request.getParameter("iscustom"));
                String action = "added";
                if (isEdit == true) {
                    action = "updated";
                }
                String master = "dimension";
                 auditaction=(action.equalsIgnoreCase("added")?AuditAction.DIMENTION_ADDED:AuditAction.DIMENTION_UPDATED);
                if (iscustom == true) {
                    if (customcolumn.equals("1")) {
                        master = "custom column";
                        auditaction=(action.equalsIgnoreCase("added")?AuditAction.CUSTOM_COLUMN_ADDED:AuditAction.CUSTOM_COLUMN_UPDATED);
                    } else {
                        master = "custom field";
                        auditaction=(action.equalsIgnoreCase("added")?AuditAction.CUSTOM_FIELD_ADDED:AuditAction.CUSTOM_FIELD_UPDATED);
                    }

                }
                auditTrailObj.insertAuditLog(auditaction, "User " + sessionHandlerImpl.getUserFullName(request) + " has " + action + " master item " + name + " for " + master + " " + masterGroup, request, fieldComboData.getId());
                txnManager.commit(status);
            } else {
                issuccess = true;
                msg = result.getMsg();
            }  
            /**
             *Adding MultiEntity related custom data (MultiEntityDimensionCustomData) 
             */                        
            if (!StringUtil.isNullOrEmpty(request.getParameter("customfield"))) {                
                    String companyid = sessionHandlerImpl.getCompanyid(request);
                    String entityvalue = request.getParameter("name");
                    String groupname = request.getParameter("groupname");
                    String id = fieldComboData.getId();
                    jobj.put("groupname", groupname.replaceAll("\\*", ""));
                    jobj.put(Constants.companyKey, companyid);
                    jobj.put("entityvalue", entityvalue);
                    jobj.put("customfield", request.getParameter("customfield"));
                    jobj.put("id", id);
                    accMasterItemsService.saveMultiEntityDimCustomDataJSON(jobj);                
            }
                //*****************************************Propagate masteritem In child companies**************************
            String auditID = "";
            boolean propagateTOChildCompaniesFalg = false;
            String childCompanyName = "";
            
            if (!StringUtil.isNullOrEmpty(request.getParameter("ispropagatetochildcompanyflag"))) {
                propagateTOChildCompaniesFalg = Boolean.parseBoolean(request.getParameter("ispropagatetochildcompanyflag"));
            }
            if (propagateTOChildCompaniesFalg) {
            
                try {
                    String parentcompanyid = sessionHandlerImpl.getCompanyid(request);
                    if (!isEdit) {
                        List childCompaniesList = companyDetailsDAOObj.getChildCompanies(parentcompanyid);

                        auditID = auditaction;
                        for (Object childObj : childCompaniesList) {
                            try {
                                status = txnManager.getTransaction(def);
                                Object[] childdataOBj = (Object[]) childObj;
                                String childCompanyID = (String) childdataOBj[0];
                                childCompanyName = (String) childdataOBj[1];
                                propagateMasterItemForCustom(request,isEdit, childCompanyID, parentcompanyid,fieldComboData.getId(),"");
                                txnManager.commit(status);
                                status = null;
                                auditTrailObj.insertAuditLog(auditID, "User " + sessionHandlerImpl.getUserFullName(request) + " has propagated(added) masteritem" + fieldComboData.getValue() + " to child company " + childCompanyName, request, fieldComboData.getId());
                            } catch (Exception ex) {
                                txnManager.rollback(status);
                                auditTrailObj.insertAuditLog(auditID, "User " + sessionHandlerImpl.getUserFullName(request) + " failed to  propagate(add) masteritem" + fieldComboData.getValue() + " to child company " + childCompanyName, request, fieldComboData.getId());
                            }
                        }
                    }else{
                    auditID = auditaction;
                        HashMap<String, Object> requestParams = new HashMap<String, Object>();
                        
                        HashMap<String, Object> filterRequestParams = new HashMap<String, Object>();
                        ArrayList filter_names = new ArrayList(), filter_params = new ArrayList();
                        filter_names.add("propagatedfieldcomboID.id");
                        filter_params.add(fieldComboData.getId());
                       
                        filterRequestParams.put("filter_names", filter_names);
                        filterRequestParams.put("filter_params", filter_params);
                        KwlReturnObject cntResult = accMasterItemsDAOobj.getMasterItemsForCustom(filterRequestParams);
                        List childCompaniesFieldComboDataList = cntResult.getEntityList();
                        if (childCompaniesFieldComboDataList.size() < 1) {
                            addPropagetedCustomDataInChild(request, def, isEdit, parentcompanyid, fieldComboData.getId(), fieldComboData.getValue(), auditaction);
                        } else {
                            Company company = null;
                            for (Object childObj : childCompaniesFieldComboDataList) {
                                try {
                                    FieldComboData childfieldComboData = (FieldComboData) childObj;
                                    if (childfieldComboData != null) {
                                        status = txnManager.getTransaction(def);
                                        String childcompanyfieldcombodataid = childfieldComboData.getId();
                                        String childCompanyID = childfieldComboData.getField().getCompanyid();
                                        company = (Company) kwlCommonTablesDAOObj.getClassObject(Company.class.getName(), childCompanyID);
                                        propagateMasterItemForCustom(request, isEdit, childCompanyID, parentcompanyid, fieldComboData.getId(), childcompanyfieldcombodataid);
//                                    
                                        txnManager.commit(status);
                                        status = null;
                                        auditTrailObj.insertAuditLog(auditID, "User " + sessionHandlerImpl.getUserFullName(request) + " has propagated(updated) masteritem" + fieldComboData.getValue() + " to child company " + company.getCompanyName(), request, fieldComboData.getId());
                                    }
                                } catch (Exception ex) {
                                    txnManager.rollback(status);
                                    auditTrailObj.insertAuditLog(auditID, "User " + sessionHandlerImpl.getUserFullName(request) + " failed to  propagate(update) masteritem" + fieldComboData.getValue() + " to child company " + company.getCompanyName(), request, fieldComboData.getId());
                                }
                            }
                        }
                    
                    }
                } catch (Exception ex) {
                    if (status != null) {
                        txnManager.rollback(status);
                    }
                }
            }
             //*****************************************Propagate masteritem In child companies Ends Here**************************
        } catch (Exception ex) {
            txnManager.rollback(status);
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
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public void addPropagetedCustomDataInChild(HttpServletRequest request, DefaultTransactionDefinition def, boolean isEdit, String parentcompanyid, String fieldcombodataid, String fieldcombodataValue, String auditaction) throws ServiceException {
        TransactionStatus status = null;
        try {
            isEdit = false;//  control comes in this metohd only in add case due to which isEdit flag is kept false.
            List childCompaniesList = companyDetailsDAOObj.getChildCompanies(parentcompanyid);
            auditID = auditaction;
            String childCompanyName = "";
            for (Object childObj : childCompaniesList) {
                try {
                    status = txnManager.getTransaction(def);
                    Object[] childdataOBj = (Object[]) childObj;
                    String childCompanyID = (String) childdataOBj[0];
                    childCompanyName = (String) childdataOBj[1];
                    propagateMasterItemForCustom(request, isEdit, childCompanyID, parentcompanyid, fieldcombodataid, "");
                    txnManager.commit(status);
                    status = null;
                    auditTrailObj.insertAuditLog(auditID, "User " + sessionHandlerImpl.getUserFullName(request) + " has propagated(added) masteritem " + fieldcombodataValue + " to child company " + childCompanyName, request, fieldcombodataid);
                } catch (Exception ex) {
                    txnManager.rollback(status);
                    auditTrailObj.insertAuditLog(auditID, "User " + sessionHandlerImpl.getUserFullName(request) + " failed to  propagate(add) masteritem " + fieldcombodataValue + " to child company " + childCompanyName, request, fieldcombodataid);
                }
            }
        } catch (Exception ex) {
            if (status != null) {
                txnManager.rollback(status);
            }
            Logger.getLogger(accMasterItemsController.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE("accMasterItemsController.addPropagetedCustomDataInChild", ex);

        }
    }
    public KwlReturnObject saveMasterPriceDependentItem(HttpServletRequest request) throws SessionExpiredException, ServiceException {
        KwlReturnObject result = null;
        String msg = messageSource.getMessage("acc.master.save", null, RequestContextUtils.getLocale(request));   //"Master item has been saved successfully";
        boolean isPresent = false;
        String itemID = request.getParameter("id");
        HashMap requestParam = AccountingManager.getGlobalParams(request);
        requestParam.put("id", itemID);
        requestParam.put("name", request.getParameter("name"));
        requestParam.put("groupid", request.getParameter("groupid"));
        requestParam.put("typeid", request.getParameter("typeid"));

        HashMap<String, Object> filterRequestParams = new HashMap<String, Object>();
        ArrayList filter_names = new ArrayList(), filter_params = new ArrayList();

        filter_names.add("company.companyID");
        filter_params.add(requestParam.get("companyid"));
        filter_names.add("value");
        filter_params.add(request.getParameter("name"));
        filter_names.add("type");
        filter_params.add((Integer.parseInt(request.getParameter("typeid"))));
        filterRequestParams.put("filter_names", filter_names);
        filterRequestParams.put("filter_params", filter_params);
        KwlReturnObject cntResult = accMasterItemsDAOobj.getMasterPriceDependentItem(filterRequestParams);
        int count = cntResult.getRecordTotalCount();

        if (count == 1) {
            String recordID = ((PriceType) cntResult.getEntityList().get(0)).getID();
            isPresent = itemID.equals(recordID) ? false : true; //Allow Editing same record
        } else if (count > 1) {
            isPresent = true;
        }

        if (isPresent) {
            msg = "Price Dependent item entry for <b>" + request.getParameter("name") + "</b> already exists.";
            return new KwlReturnObject(false, msg, null, null, 0);
        } else {
            if (StringUtil.isNullOrEmpty(itemID)) {
                result = accMasterItemsDAOobj.addMasterPriceDependentItem(requestParam);
            } else {
                result = accMasterItemsDAOobj.updateMasterPriceDependentItem(requestParam);
            }
        }
        return new KwlReturnObject(true, msg, null, result.getEntityList(), 0);
    }

    public ModelAndView getMasterPriceDependentItem(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        try {
            jobj = getMasterPriceDependentItem(request);
            issuccess = true;
        } catch (SessionExpiredException ex) {
            msg = ex.getMessage();
            Logger.getLogger(accMasterItemsController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ServiceException ex) {
            msg = ex.getMessage();
            Logger.getLogger(accMasterItemsController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            msg = "accMasterItemsController.getMasterPriceDependentItem : " + ex.getMessage();
            Logger.getLogger(accMasterItemsController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accMasterItemsController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public ModelAndView getMasterItemPrice(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        try {
            jobj = getMasterItemPrice(request);
            issuccess = true;
        } catch (SessionExpiredException ex) {
            msg = ex.getMessage();
            Logger.getLogger(accMasterItemsController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ServiceException ex) {
            msg = ex.getMessage();
            Logger.getLogger(accMasterItemsController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            msg = "accMasterItemsController.getMasterItemPrice : " + ex.getMessage();
            Logger.getLogger(accMasterItemsController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accMasterItemsController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public ModelAndView getMasterItemPriceFormula(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        try {
            jobj = getMasterItemPriceFormula(request);
            issuccess = true;
        } catch (SessionExpiredException ex) {
            msg = ex.getMessage();
            Logger.getLogger(accMasterItemsController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ServiceException ex) {
            msg = ex.getMessage();
            Logger.getLogger(accMasterItemsController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            msg = "accMasterItemsController.getMasterItemPriceFormula : " + ex.getMessage();
            Logger.getLogger(accMasterItemsController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accMasterItemsController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public JSONObject getMasterItemPrice(HttpServletRequest request) throws ServiceException, SessionExpiredException {
        JSONObject jobj = new JSONObject();
        try {
            HashMap<String, Object> filterRequestParams = new HashMap<String, Object>();
            ArrayList filter_names = new ArrayList(), filter_params = new ArrayList(), order_by = new ArrayList(), order_type = new ArrayList();
            if (StringUtil.isNullOrEmpty(request.getParameter("itemid"))) {
                filter_names.add("type.company.companyID");
                filter_params.add(sessionHandlerImpl.getCompanyid(request));
            } else {
                filter_names.add("type.ID");
                filter_params.add(request.getParameter("itemid"));
            }
            order_by.add("value");
            order_type.add("asc");
            filterRequestParams.put("filter_names", filter_names);
            filterRequestParams.put("filter_params", filter_params);
            filterRequestParams.put("order_by", order_by);
            filterRequestParams.put("order_type", order_type);
            KwlReturnObject result = accMasterItemsDAOobj.getMasterItemPrice(filterRequestParams);

            List list = result.getEntityList();
            Iterator itr = list.iterator();
            JSONArray jArr = new JSONArray();
            while (itr.hasNext()) {
                MasterItemPrice masterItemPrice = (MasterItemPrice) itr.next();
                JSONObject obj = new JSONObject();
                obj.put("id", masterItemPrice.getID());
                obj.put("dependentType", masterItemPrice.getID());
                obj.put("value", masterItemPrice.getValue());
                obj.put("price", masterItemPrice.getPrice());
                obj.put("type", masterItemPrice.getType().getID());
                jArr.put(obj);
            }
            jobj.put("data", jArr);
            jobj.put("count", jArr.length());
        } catch (JSONException e) {
            throw ServiceException.FAILURE(e.getMessage(), e);
        }
        return jobj;
    }

    public JSONObject getMasterItemPriceFormula(HttpServletRequest request) throws ServiceException, SessionExpiredException {
        JSONObject jobj = new JSONObject();
        try {
            HashMap<String, Object> filterRequestParams = new HashMap<String, Object>();
            ArrayList filter_names = new ArrayList(), filter_params = new ArrayList(), order_by = new ArrayList(), order_type = new ArrayList();
            filter_names.add("type.ID");
            filter_params.add(request.getParameter("itemid"));
            order_by.add("lowerlimitvalue");
            order_by.add("upperlimitvalue");
            order_type.add("asc");
            order_type.add("asc");
            filterRequestParams.put("filter_names", filter_names);
            filterRequestParams.put("filter_params", filter_params);
            filterRequestParams.put("order_by", order_by);
            filterRequestParams.put("order_type", order_type);
            KwlReturnObject result = accMasterItemsDAOobj.getMasterItemPriceFormula(filterRequestParams);

            List list = result.getEntityList();
            Iterator itr = list.iterator();
            JSONArray jArr = new JSONArray();
            while (itr.hasNext()) {
                MasterItemPriceFormula masterItemPriceFormula = (MasterItemPriceFormula) itr.next();
                JSONObject obj = new JSONObject();
                obj.put("id", masterItemPriceFormula.getID());
                obj.put("lowerlimit", masterItemPriceFormula.getLowerlimitvalue());
                obj.put("upperlimit", masterItemPriceFormula.getUpperlimitvalue());
                obj.put("base", masterItemPriceFormula.getBasevalue());
                obj.put("increment", masterItemPriceFormula.getIncvalue());
                jArr.put(obj);
            }
            jobj.put("data", jArr);
            jobj.put("count", jArr.length());
        } catch (JSONException e) {
            throw ServiceException.FAILURE(e.getMessage(), e);
        }
        return jobj;
    }

    public JSONObject getMasterPriceDependentItem(HttpServletRequest request) throws ServiceException, SessionExpiredException {
        JSONObject jobj = new JSONObject();
        try {
            HashMap<String, Object> filterRequestParams = new HashMap<String, Object>();
            ArrayList filter_names = new ArrayList(), filter_params = new ArrayList(), order_by = new ArrayList(), order_type = new ArrayList();
            filter_names.add("company.companyID");
            filter_params.add(sessionHandlerImpl.getCompanyid(request));
            order_by.add("value");
            order_type.add("asc");
            filterRequestParams.put("filter_names", filter_names);
            filterRequestParams.put("filter_params", filter_params);
            filterRequestParams.put("order_by", order_by);
            filterRequestParams.put("order_type", order_type);
            KwlReturnObject result = accMasterItemsDAOobj.getMasterPriceDependentItem(filterRequestParams);

            List list = result.getEntityList();
            Iterator itr = list.iterator();
            JSONArray jArr = new JSONArray();
            while (itr.hasNext()) {
                PriceType priceType = (PriceType) itr.next();
                JSONObject obj = new JSONObject();
                obj.put("id", priceType.getID());
                obj.put("name", priceType.getValue());
                obj.put("parentid", "");
                obj.put("type", priceType.getType());
                obj.put("leaf", true);
                obj.put("level", 0);
                jArr.put(obj);
            }
            jobj.put("data", jArr);
            jobj.put("count", jArr.length());
        } catch (JSONException e) {
            throw ServiceException.FAILURE(e.getMessage(), e);
        }
        return jobj;
    }

    public KwlReturnObject saveMasterItemPrice(HttpServletRequest request) throws SessionExpiredException, ServiceException {
        KwlReturnObject result = null;
        String msg = messageSource.getMessage("acc.master.save", null, RequestContextUtils.getLocale(request));   //"Master item has been saved successfully";
        boolean isPresent = true;
        String itemID = request.getParameter("itemid");
        HashMap requestParam = AccountingManager.getGlobalParams(request);
        requestParam.put("typeid", itemID);
        requestParam.put("name", request.getParameter("name"));
        requestParam.put("price", request.getParameter("price"));
        result = accMasterItemsDAOobj.addMasterItemPrice(requestParam);

        return new KwlReturnObject(true, msg, null, result.getEntityList(), 0);

    }

    public KwlReturnObject saveMasterItemPriceFormula(HttpServletRequest request) throws SessionExpiredException, ServiceException {
        KwlReturnObject result = null;
        String msg = messageSource.getMessage("acc.master.save", null, RequestContextUtils.getLocale(request));   //"Master item has been saved successfully";
        boolean isPresent = true;
        String itemID = request.getParameter("itemid");
        HashMap requestParam = AccountingManager.getGlobalParams(request);
        requestParam.put("typeid", itemID);
        requestParam.put("lowerlimitvalue", request.getParameter("lowerlimitvalue"));
        requestParam.put("upperlimitvalue", request.getParameter("upperlimitvalue"));
        requestParam.put("basevalue", request.getParameter("basevalue"));
        requestParam.put("incvalue", request.getParameter("incvalue"));
        result = accMasterItemsDAOobj.saveMasterItemPriceFormula(requestParam);

        return new KwlReturnObject(true, msg, null, result.getEntityList(), 0);

    }

    public KwlReturnObject saveMasterItem(HttpServletRequest request) throws SessionExpiredException, ServiceException {
        KwlReturnObject result = null;
        String msg = messageSource.getMessage("acc.master.save", null, RequestContextUtils.getLocale(request));   //"Master item has been saved successfully";
        boolean isPresent = false;
        boolean isDefaultToPOS=!StringUtil.isNullOrEmpty(request.getParameter("isDefaultToPOS"))?Boolean.parseBoolean(request.getParameter("isDefaultToPOS")):false;
        String itemID = request.getParameter("id");
        HashMap requestParam = AccountingManager.getGlobalParams(request);
        requestParam.put("id", itemID);
        requestParam.put("name", request.getParameter("name").trim());
        requestParam.put("code", request.getParameter("salespersoncode"));
        String sectionCode=request.getParameter("sectionCode");
        String groupId=request.getParameter("groupid");
        if(!StringUtil.isNullOrEmpty(sectionCode) && groupId.equals("33")){
            requestParam.put("code", sectionCode);
        }
        if(!StringUtil.isNullOrEmpty(request.getParameter("typeOfDeducteeType")) && groupId.equals("34")){ // Type of deductee type
            requestParam.put("code",request.getParameter("typeOfDeducteeType"));
        }
        requestParam.put("salesPersonContactNumber", request.getParameter("salesPersonContactNumber"));
        requestParam.put("salesPersonAddress", request.getParameter("salesPersonAddress"));
        requestParam.put("groupid", request.getParameter("groupid"));
        requestParam.put("salesPersonDesignation", request.getParameter("salesPersonDesignation"));
        String accId = request.getParameter("accid");
        if (StringUtil.isNullOrEmpty(itemID)) {
            requestParam.put("activated", true);
        } 
        boolean isIBGActivated = false;
        if (request.getParameter("groupid").equals("17")) {// for Paid to
            if (!StringUtil.isNullOrEmpty(request.getParameter("isIBGActivated"))) {
                isIBGActivated = Boolean.parseBoolean(request.getParameter("isIBGActivated"));
            }
        }
        requestParam.put("isIBGActivated", isIBGActivated);
        String parentId = request.getParameter("parentid");
        if (!StringUtil.isNullOrEmpty(parentId)) {
            requestParam.put("parentid", parentId);
        }
        String emailid = request.getParameter("emailid");
        String userId = request.getParameter("userid");
        if (!StringUtil.isNullOrEmpty(emailid)) {
            requestParam.put("emailid", emailid);
        }else{
            requestParam.put("emailid", emailid);
        }
        if (!StringUtil.isNullOrEmpty(userId)) {
            requestParam.put("userid", userId);
        }
        if (!StringUtil.isNullOrEmpty(accId)) {
            requestParam.put("accid", accId);
        }
        if (!StringUtil.isNullOrEmpty(request.getParameter("custVendCategoryTypeId"))) {
            int custVendCategoryTypeId = Integer.parseInt(request.getParameter("custVendCategoryTypeId"));
            requestParam.put("custVendCategoryTypeId", custVendCategoryTypeId);
        }
        if (!StringUtil.isNullOrEmpty(request.getParameter("driverID"))) {
            requestParam.put("driverID", request.getParameter("driverID"));
        }
        if (!StringUtil.isNullOrEmpty(request.getParameter("isDefaultToPOS"))) {
            requestParam.put("isDefaultToPOS", isDefaultToPOS);
        }
        if (!StringUtil.isNullOrEmpty(request.getParameter("variancePercentage"))) {
            requestParam.put("variancePercentage", Double.parseDouble(request.getParameter("variancePercentage")));
        }
        if (!StringUtil.isNullOrEmpty(request.getParameter("industryCodeId"))) {
            requestParam.put("industryCodeId", request.getParameter("industryCodeId"));
        }
        if (!StringUtil.isNullOrEmpty(request.getParameter("vatcommoditycode"))) {
            requestParam.put("vatcommoditycode", (request.getParameter("vatcommoditycode")));
        }
        if (!StringUtil.isNullOrEmpty(request.getParameter("vatscheduleno"))) {
            requestParam.put("vatscheduleno", (request.getParameter("vatscheduleno")));
        }
        if (!StringUtil.isNullOrEmpty(request.getParameter("vatscheduleserialno"))) {
            requestParam.put("vatscheduleserialno", (request.getParameter("vatscheduleserialno")));
        }
        if (!StringUtil.isNullOrEmpty(request.getParameter("vatnotes"))) {
            requestParam.put("vatnotes", (request.getParameter("vatnotes")));
        }
        if (!StringUtil.isNullOrEmpty(request.getParameter("landingcostallocationtype"))) {
            requestParam.put("lcallocationid", (request.getParameter("landingcostallocationtype")));
        }
        if (!StringUtil.isNullOrEmpty(request.getParameter("landingcostcategory"))) {
            requestParam.put("lccategoryid", (request.getParameter("landingcostcategory")));
        }
        if (!StringUtil.isNullOrEmpty(request.getParameter("BICCode"))) {
            requestParam.put("BICCode", (request.getParameter("BICCode")));
        }
        if (!StringUtil.isNullOrEmpty(request.getParameter("bankCode"))) {
            requestParam.put("bankCode", (request.getParameter("bankCode")));
        }
        if (!StringUtil.isNullOrEmpty(request.getParameter("branchCode"))) {
            requestParam.put("branchCode", (request.getParameter("branchCode")));
        }
        if (!StringUtil.isNullOrEmpty(request.getParameter("isAppendBranchCode"))) {
            requestParam.put("isAppendBranchCode", (request.getParameter("isAppendBranchCode")));
        }
        HashMap<String, Object> filterRequestParams = new HashMap<String, Object>();
        ArrayList filter_names = new ArrayList(), filter_params = new ArrayList();
        filter_names.add("masterGroup.ID");
        filter_params.add(request.getParameter("groupid"));
        filter_names.add("company.companyID");
        filter_params.add(requestParam.get("companyid"));
        filter_names.add("value");
        filter_params.add(request.getParameter("name"));
        filterRequestParams.put("filter_names", filter_names);
        filterRequestParams.put("filter_params", filter_params);
        KwlReturnObject cntResult = accMasterItemsDAOobj.getMasterItems(filterRequestParams);
        int count = cntResult.getRecordTotalCount();

        if (count == 1) {
            String recordID = ((MasterItem) cntResult.getEntityList().get(0)).getID();
            isPresent = itemID.equals(recordID) ? false : true; //Allow Editing same record
        } else if (count > 1) {
            isPresent = true;
        }
        MasterItem pref = (MasterItem) kwlCommonTablesDAOObj.getClassObject(MasterItem.class.getName(), itemID);
        boolean isCyclic = false;
        if (!StringUtil.isNullOrEmpty(parentId)) {
            if (pref != null && pref.getChildren().size() > 0) {
                Iterator<MasterItem> chieldValues = pref.getChildren().iterator();
                isCyclic = getCyclicMasterItem(chieldValues, parentId, isCyclic);
            }
        }
        if (!isCyclic) {
            if (isPresent) {
                msg = "<b>" + request.getParameter("name") + "</b>:" + messageSource.getMessage("acc.master.configuration.already.exists", null, RequestContextUtils.getLocale(request));
                return new KwlReturnObject(false, msg, null, null, 0);
            } else {
                 if (isDefaultToPOS) {
                ArrayList filter_names1 = new ArrayList(), filter_params1 = new ArrayList();
                 HashMap<String, Object> filterRequestParams1 = new HashMap<String, Object>();
                filter_names1.add("masterGroup.ID");
                filter_params1.add("19");
                filter_names1.add("company.companyID");
                filter_params1.add(sessionHandlerImpl.getCompanyid(request));
                filter_names1.add("defaultToPOS");
                filter_params1.add(true);
                filterRequestParams1.put("filter_names", filter_names1);
                filterRequestParams1.put("filter_params", filter_params1);
                cntResult = accMasterItemsDAOobj.getMasterItems(filterRequestParams1);
                
                if (cntResult.getEntityList() != null && !cntResult.getEntityList().isEmpty()) {
                    MasterItem mi = (MasterItem) cntResult.getEntityList().get(0);
                    
                    HashMap resetRequestParam = AccountingManager.getGlobalParams(request);
                    resetRequestParam.put("id", mi.getID());
                    resetRequestParam.put("isDefaultToPOS", false);
                    resetRequestParam.put("companyid", sessionHandlerImpl.getCompanyid(request));
                    
                    accMasterItemsDAOobj.updateMasterItem(resetRequestParam);
                }
            }
                if (StringUtil.isNullOrEmpty(itemID)) {
                    result = accMasterItemsDAOobj.addMasterItem(requestParam);
                } else {
                    result = accMasterItemsDAOobj.updateMasterItem(requestParam);
                }
                result.getEntityList().add(requestParam);
            }
        } else {
            msg = "Sorry, you cannot set child as a parent.";
            result = cntResult;
        }
        return new KwlReturnObject(true, msg, null, result.getEntityList(), 0);
    }

    public KwlReturnObject saveLocationItem(HttpServletRequest request) throws SessionExpiredException, ServiceException {
        KwlReturnObject result = null;
        String msg = messageSource.getMessage("acc.master.save", null, RequestContextUtils.getLocale(request));   //"Master item has been saved successfully";
        boolean isPresent = false;
        String itemID = request.getParameter("id");
        HashMap requestParam = AccountingManager.getGlobalParams(request);
        requestParam.put("id", itemID);
        requestParam.put("name", request.getParameter("name"));
        requestParam.put("parent", request.getParameter("parent"));
        requestParam.put("isdefault", false);
        requestParam.put("parentid", request.getParameter("parentid"));
        HashMap<String, Object> filterRequestParams = new HashMap<String, Object>();
        ArrayList filter_names = new ArrayList(), filter_params = new ArrayList();
        filter_names.add("company.companyID");
        filter_params.add(requestParam.get("companyid"));
        filter_names.add("name");
        filter_params.add(request.getParameter("name"));
        filterRequestParams.put("filter_names", filter_names);
        filterRequestParams.put("filter_params", filter_params);
        String userId = sessionHandlerImpl.getUserid(request);
        KwlReturnObject jeresult = accountingHandlerDAOobj.getObject(User.class.getName(), userId);
        User user = (User) jeresult.getEntityList().get(0);
        requestParam.put("user",user);
        KwlReturnObject cntResult = accMasterItemsDAOobj.getLocationItems(filterRequestParams);
        int count = cntResult.getRecordTotalCount();
        if (count == 1) {
            String recordID = ((InventoryLocation) cntResult.getEntityList().get(0)).getId();
            isPresent = itemID.equals(recordID) ? false : true; //Allow Editing same record
        } else if (count > 1) {
            isPresent = true;
        }

        if (isPresent) {
            msg = "Master item entry for <b>" + request.getParameter("name") + "</b> already exists.";
            return new KwlReturnObject(false, msg, null, null, 0);
        } else {
            result = accMasterItemsDAOobj.addLocationItem(requestParam);
        }
        return new KwlReturnObject(true, msg, null, result.getEntityList(), 0);
    }
    
        public KwlReturnObject saveDepartmentItem(HttpServletRequest request) throws SessionExpiredException, ServiceException {
        KwlReturnObject result = null;
        String msg = messageSource.getMessage("acc.master.save", null, RequestContextUtils.getLocale(request));   //"Master item has been saved successfully";
        boolean isPresent = false;
        String itemID = request.getParameter("id");
        HashMap requestParam = AccountingManager.getGlobalParams(request);
        requestParam.put("id", itemID);
        requestParam.put("name", request.getParameter("name"));
//        requestParam.put("parent", request.getParameter("parent"));
        requestParam.put("parentid", request.getParameter("parentid"));
        HashMap<String, Object> filterRequestParams = new HashMap<String, Object>();
        ArrayList filter_names = new ArrayList(), filter_params = new ArrayList();
        filter_names.add("company.companyID");
        filter_params.add(requestParam.get("companyid"));
        filter_names.add("name");
        filter_params.add(request.getParameter("name"));
        filterRequestParams.put("filter_names", filter_names);
        filterRequestParams.put("filter_params", filter_params);
        KwlReturnObject cntResult = accMasterItemsDAOobj.getDepartments(filterRequestParams);
        int count = cntResult.getRecordTotalCount();

        if (count == 1) {
            String recordID = ((Department) cntResult.getEntityList().get(0)).getId();
            isPresent = itemID.equals(recordID) ? false : true; //Allow Editing same record
        } else if (count > 1) {
            isPresent = true;
        }

        if (isPresent) {
            msg = "<b>" + request.getParameter("name") + "</b>:" + messageSource.getMessage("acc.masteritem.entry.already.exist", null, RequestContextUtils.getLocale(request));
            return new KwlReturnObject(false, msg, null, null, 0);
        } else {
            result = accMasterItemsDAOobj.saveDepartmentItem(requestParam);
        }
        return new KwlReturnObject(true, msg, null, result.getEntityList(), 0);
    }

    public KwlReturnObject saveWarehouseItem(HttpServletRequest request) throws SessionExpiredException, ServiceException {
        KwlReturnObject result = null;
        String msg = messageSource.getMessage("acc.master.save", null, RequestContextUtils.getLocale(request));   //"Master item has been saved successfully";
        boolean isPresent = false;
        String itemID = request.getParameter("id");
        HashMap requestParam = AccountingManager.getGlobalParams(request);
        requestParam.put("id", itemID);
        requestParam.put("name", request.getParameter("name"));
        requestParam.put("parent", request.getParameter("parent"));
        requestParam.put("location", request.getParameter("location"));
        requestParam.put("isdefault", false);
        requestParam.put("parentid", request.getParameter("parentid"));
        HashMap<String, Object> filterRequestParams = new HashMap<String, Object>();
        ArrayList filter_names = new ArrayList(), filter_params = new ArrayList();
        filter_names.add("company.companyID");
        filter_params.add(requestParam.get("companyid"));
        filter_names.add("name");
        filter_params.add(request.getParameter("name"));
        filterRequestParams.put("filter_names", filter_names);
        filterRequestParams.put("filter_params", filter_params);
        KwlReturnObject cntResult = accMasterItemsDAOobj.getWarehouseItems(filterRequestParams);
        int count = cntResult.getRecordTotalCount();

        if (count == 1) {
            String recordID = ((InventoryWarehouse) cntResult.getEntityList().get(0)).getId();
            isPresent = itemID.equals(recordID) ? false : true; //Allow Editing same record
        } else if (count > 1) {
            isPresent = true;
        }

        if (isPresent) {
            msg = "WareHouse item entry for <b>" + request.getParameter("name") + "</b> already exists.";
            return new KwlReturnObject(false, msg, null, null, 0);
        } else {
            result = accMasterItemsDAOobj.addWarehouseItem(requestParam);
        }
        return new KwlReturnObject(true, msg, null, result.getEntityList(), 0);
    }
    
    public KwlReturnObject propagateMasterItemForCustom(HttpServletRequest request,boolean isEdit,String childCompanyID,String parentcompanyid,String parentCompanysFieldCombodataId,String childcompanyfieldcombodataid) throws SessionExpiredException, ServiceException {
        KwlReturnObject result = null;
        KwlReturnObject result1 = null;
        JSONArray syncArray = new JSONArray();
        FieldComboData combo = null;
        String msg = messageSource.getMessage("acc.master.save", null, RequestContextUtils.getLocale(request));   //"Master item has been saved successfully";
        boolean isPresent = false;
        
        String itemID = childcompanyfieldcombodataid;//edit case 
        String valuename=request.getParameter("name");
        String description =request.getParameter("itemdescription");
        String parentId = request.getParameter("parentid");
        String parentValueid = request.getParameter("parentValueid");
        
        String moduleIds = request.getParameter("moduleIds");
        String fieldidsofmodules[] = moduleIds.split(",");
        
        HashMap requestParam = AccountingManager.getGlobalParams(request);
        requestParam.put(Constants.companyKey, childCompanyID);
        requestParam.put("parentCompanysFieldCombodataId",parentCompanysFieldCombodataId);
        
         String table = "", dataColumn = "", fetchColumn = "";
        table = "FieldComboData";
        fetchColumn = "value";
        dataColumn = "id";
        List dataList = null;
            String data = parentId;
            HashMap<String, Object> requestParams1 = new HashMap<String, Object>();
            requestParams1.put("companyid", parentcompanyid);
            try {
                dataList = importHandler.getRefData(requestParams1, table, dataColumn, fetchColumn, "", data);
                data = (String) dataList.get(0);
                //get id from name .example - select id from account where name=?
                requestParams1.put("companyid", childCompanyID);
                dataList = importHandler.getRefData(requestParams1, table, fetchColumn, dataColumn, "", data);
                data = (String) dataList.get(0);
                if (!StringUtil.isNullOrEmpty(data)) {
                    parentId=data;
                }

            } catch (Exception ex) {
                Logger.getLogger(accMasterItemsController.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            
        String[] parentvalueidsArr = new String[parentValueid.split(",").length];
        String parentvalueidsString="";
        for (int i = 0; i < parentvalueidsArr.length; i++) {
            dataList = null;
            data = parentvalueidsArr[i];
            requestParams1 = new HashMap<String, Object>();
            requestParams1.put("companyid", parentcompanyid);
            try {
                dataList = importHandler.getRefData(requestParams1, table, dataColumn, fetchColumn, "", data);
                data = (String) dataList.get(0);
                //get id from name .example - select id from account where name=?
                requestParams1.put("companyid", childCompanyID);
                dataList = importHandler.getRefData(requestParams1, table, fetchColumn, dataColumn, "", data);
                data = (String) dataList.get(0);
                if (!StringUtil.isNullOrEmpty(data)) {
                    parentvalueidsString+= data +",";
                }

            } catch (Exception ex) {
                 Logger.getLogger(accMasterItemsController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        if (!StringUtil.isNullOrEmpty(parentvalueidsString)) {
            
            parentValueid =parentvalueidsString.substring(0, (parentvalueidsString.length()-1));
        }
                
        table = "FieldParams";
        fetchColumn = "fieldlabel";
        dataColumn = "id";
       String []comIds=new String[fieldidsofmodules.length];
       for (int i = 0; i < fieldidsofmodules.length; i++) {
            dataList = null;
            data = fieldidsofmodules[i];
            requestParams1 = new HashMap<String, Object>();
            requestParams1.put("companyid", parentcompanyid);
            try {
                dataList = importHandler.getRefData(requestParams1, table, dataColumn, fetchColumn, "", data);
                data = (String) dataList.get(0);
                //get id from name .example - select id from account where name=?
                requestParams1.put("companyid", childCompanyID);
                dataList = importHandler.getRefData(requestParams1, table, fetchColumn, dataColumn, "", data);
                data = (String) dataList.get(0);
                if (!StringUtil.isNullOrEmpty(data)) {
                    comIds[i]=data;
                }

            } catch (Exception ex) {
//                
            }
       }
       
        HashMap<String, String> parentIdsEdit = null;
        if (isEdit) {
            parentIdsEdit = accMasterItemsDAOobj.getParentIds(comIds, itemID);
        }
        HashMap<String, String> paresntIdsMap = null;
        if (!parentId.equals("") && !parentId.equals("-1")) {
            paresntIdsMap = accMasterItemsDAOobj.getParentIds(comIds, parentId);
        }
        for (int i = 0; i < comIds.length; i++) {
            if (!parentId.equals("") && !parentId.equals("-1")) {
                parentId = paresntIdsMap.get(comIds[i]);
            }
            if (isEdit) {
                itemID = parentIdsEdit.get(comIds[i]);
            }
            //requestParam.put("id", itemID);
            requestParam.put("id", itemID.equals("") ? itemID : parentIdsEdit.get(comIds[i]));
            requestParam.put("name", valuename);
            //requestParam.put("groupid", request.getParameter("groupid"));
            requestParam.put("groupid", comIds[i]);
            requestParam.put("parentid", parentId);
            if (!StringUtil.isNullOrEmpty(description)) {
                requestParam.put("itemdescription",description);
            }
            HashMap<String, Object> filterRequestParams = new HashMap<String, Object>();
            ArrayList filter_names = new ArrayList(), filter_params = new ArrayList();
            filter_names.add("field.id");
            //filter_params.add(request.getParameter("groupid"));
            filter_params.add(comIds[i]);
            filter_names.add("value");
            filter_params.add(valuename);
            filterRequestParams.put("filter_names", filter_names);
            filterRequestParams.put("filter_params", filter_params);

            FieldComboData pref = (FieldComboData) kwlCommonTablesDAOObj.getClassObject(FieldComboData.class.getName(), itemID);
            combo = pref;

            boolean isCyclic = false;
            if (!StringUtil.isNullOrEmpty(parentId)) {
                if (pref != null && pref.getChildren().size() > 0) {
                    Iterator<FieldComboData> chieldValues = pref.getChildren().iterator();
                    isCyclic = getCyclic(chieldValues, parentId, isCyclic);
                }
            }

            KwlReturnObject cntResult = accMasterItemsDAOobj.getMasterItemsForCustom(filterRequestParams);
            if (!isCyclic) {
                int count = cntResult.getRecordTotalCount();

                if (count == 1) {
                    String testParentId = "";
                    String recordID = ((FieldComboData) cntResult.getEntityList().get(0)).getId();
                    FieldComboData testParent = ((FieldComboData) cntResult.getEntityList().get(0)).getParent();
                    if (testParent != null) {
                        testParentId = testParent.getId();
                    }
                    isPresent = itemID.equals(recordID) ? false : true; //Allow Editing same record
                    result = cntResult;
                } else if (count > 1) {
                    isPresent = true;
                }

                if (isPresent) {
                    msg = "Master item entry for <b>" + valuename + "</b> already exists.";
                } else {

                    result = accMasterItemsDAOobj.addUpdateMasterCustomItem(requestParam, isEdit);
                    FieldComboData fieldComboData = ((FieldComboData) result.getEntityList().get(0));
                    combo = fieldComboData;
                    String recordID = fieldComboData.getId();
                    requestParam.put("chieldValueId", recordID);
                    result1 = accMasterItemsDAOobj.deleteMasterCustomItemMapping(recordID);
                    if (!StringUtil.isNullOrEmpty(parentValueid)) {
                        String parentValue = accMasterItemsService.getParentItemsForMap(fieldComboData.getField(), parentValueid);
                        if (!parentValue.isEmpty()) {
                            String parentValueArray[] = parentValue.split(",");
                            for (int cnt = 0; cnt < parentValueArray.length; cnt++) {
                                requestParam.put("parentValueid", parentValueArray[cnt]);
                                result1 = accMasterItemsDAOobj.addUpdateMasterCustomItemMapping(requestParam);
                            }
                        }
                    }
                }
            } else {
                msg = "Sorry, you cannot set child as a parent.";
                result = cntResult;
            }
            isPresent = false;
        }
        if (result != null) {
            return new KwlReturnObject(true, msg, null, result.getEntityList(), 0);
        } else {
            return new KwlReturnObject(false, msg, null, null, 0);
        }
    }
    public KwlReturnObject saveMasterItemForCustom(HttpServletRequest request) throws SessionExpiredException, ServiceException,AccountingException {
        KwlReturnObject result = null;
        KwlReturnObject result1 = null;
        JSONArray syncArray = new JSONArray();
        FieldComboData combo = null;
        String msg = messageSource.getMessage("acc.master.save", null, RequestContextUtils.getLocale(request));   //"Master item has been saved successfully";
        boolean isPresent = false;
        String itemID = request.getParameter("id");
        String parentId = request.getParameter("parentid");
        String parentValueid = request.getParameter("parentValueid");
        boolean isEdit = Boolean.parseBoolean(request.getParameter("isEdit"));
        String moduleIds = request.getParameter("moduleIds");
        String sequenceformat = request.getParameter("sequenceformat");
        String comIds[] = moduleIds.split(",");
        String name = request.getParameter("name");
        String custom = request.getParameter("groupid");
        String language=RequestContextUtils.getLocale(request).getLanguage()+"_"+RequestContextUtils.getLocale(request).getCountry();
        HashMap<String, String> parentIdsEdit = null;
        if (isEdit) {
            parentIdsEdit = accMasterItemsDAOobj.getParentIds(comIds, itemID);
        }
        HashMap<String, String> paresntIdsMap = null;
        if (!parentId.equals("") &&! parentId.equals("-1")) {
            paresntIdsMap = accMasterItemsDAOobj.getParentIds(comIds, parentId);
        }
        Map<String, Object> seqNumberMap = new HashMap<>();
        for (int i = 0; i < comIds.length; i++) {
            if (!parentId.equals("") &&! parentId.equals("-1")) {
                parentId = paresntIdsMap.get(comIds[i]);
            }
            if (isEdit) {
                itemID = parentIdsEdit.get(comIds[i]);
            }
            if (!sequenceformat.equals("NA") && !isEdit) {
                seqNumberMap = accMasterItemsDAOobj.getNextAutoNumber_Modified(comIds[i], StaticValues.AUTONUM_DIMENSION, sequenceformat, false, Calendar.getInstance().getTime());
            }
            HashMap requestParam = AccountingManager.getGlobalParams(request);
            //requestParam.put("id", itemID);
            List list = accMasterItemsDAOobj.checksEntryNumberForSequenceNumber(Constants.Acc_Dimension_ModuleId, name,custom);
            if (sequenceformat.equals("NA") && !list.isEmpty()) {
                boolean isvalidEntryNumber = (Boolean) list.get(0);
                String formatName = (String) list.get(1);
                if (!isvalidEntryNumber) {
                    throw new AccountingException(messageSource.getMessage("acc.common.enterdocumentnumber", null, Locale.forLanguageTag(language)) + " <b>" + name + "</b> " + messageSource.getMessage("acc.common.belongsto", null, Locale.forLanguageTag(language)) + " <b>" + formatName + "</b>. " + messageSource.getMessage("acc.common.plselectseqformat", null, Locale.forLanguageTag(language)) + " <b>" + formatName + "</b> " + messageSource.getMessage("acc.common.insteadof", null, Locale.forLanguageTag(language)));
                }
            }
            if(!isEdit && !sequenceformat.equals("NA")){
                name=seqNumberMap.get(Constants.AUTO_ENTRYNUMBER).toString();
            }
            requestParam.put("id", StringUtil.isNullOrEmpty(itemID) ? "" : parentIdsEdit.get(comIds[i]));
            requestParam.put("name", name);
            if(!sequenceformat.equals("NA") && !isEdit){
                requestParam.put("seqformat", sequenceformat);
                requestParam.put("seqnumber", seqNumberMap.get(Constants.SEQNUMBER).toString());
                requestParam.put("autogenerated", sequenceformat.equals("NA") ? false : true);
            }
            //requestParam.put("groupid", request.getParameter("groupid"));
            requestParam.put("groupid", comIds[i]);
            requestParam.put("parentid", parentId);
            requestParam.put("activatedeactivateflg", true);
            if (!StringUtil.isNullObject(request.getParameter("itemdescription"))) {
               requestParam.put("itemdescription", request.getParameter("itemdescription"));
            }
            HashMap<String, Object> filterRequestParams = new HashMap<String, Object>();
            ArrayList filter_names = new ArrayList(), filter_params = new ArrayList();
            filter_names.add("field.id");
            //filter_params.add(request.getParameter("groupid"));
            filter_params.add(comIds[i]);
            filter_names.add("value");
            filter_params.add(request.getParameter("name"));
            filterRequestParams.put("filter_names", filter_names);
            filterRequestParams.put("filter_params", filter_params);

            FieldComboData pref = (StringUtil.isNullOrEmpty(itemID) ? null : (FieldComboData) kwlCommonTablesDAOObj.getClassObject(FieldComboData.class.getName(), itemID));
            combo = pref;
            
            boolean isCyclic = false;
            if (!StringUtil.isNullOrEmpty(parentId)) {
                if (pref != null && pref.getChildren().size() > 0) {
                    Iterator<FieldComboData> chieldValues = pref.getChildren().iterator();
                    isCyclic = getCyclic(chieldValues, parentId, isCyclic);
                }
            }

            KwlReturnObject cntResult = accMasterItemsDAOobj.getMasterItemsForCustom(filterRequestParams);
            if (!isCyclic) {
                int count = cntResult.getRecordTotalCount();

                if (count == 1) {
                    String testParentId = "";
                    String recordID = ((FieldComboData) cntResult.getEntityList().get(0)).getId();
                    FieldComboData testParent = ((FieldComboData) cntResult.getEntityList().get(0)).getParent();
                    if (testParent != null) {
                        testParentId = testParent.getId();
                    }
                    isPresent = itemID.equals(recordID) ? false : true; //Allow Editing same record
                    result = cntResult;
                } else if (count > 1) {
                    isPresent = true;
                }

                if (isPresent) {
                    msg = "Master item entry for <b>" + request.getParameter("name") + "</b> already exists.";
                    } else {

                    result = accMasterItemsDAOobj.addUpdateMasterCustomItem(requestParam, isEdit);
                    FieldComboData fieldComboData = ((FieldComboData) result.getEntityList().get(0));
                    combo = fieldComboData;
                    String recordID = fieldComboData.getId();
                    String fieldLabel=fieldComboData.getField().getFieldlabel();
                    requestParam.put("chieldValueId", recordID);
                    requestParam.put("fieldLabel", fieldLabel);                    
                    result1 = accMasterItemsDAOobj.deleteMasterCustomItemMapping(recordID);
                  if (!StringUtil.isNullOrEmpty(parentValueid) && fieldComboData.getField().getParent()!=null) {//This block will execute only for those dimensions which have parent
                        String parentValue = accMasterItemsService.getParentItemsForMap(fieldComboData.getField(), parentValueid);
                        if (!parentValue.isEmpty()) {
                        String parentValueArray[] = parentValue.split(",");
                        for (int cnt = 0; cnt < parentValueArray.length; cnt++) {
                            requestParam.put("parentValueid", parentValueArray[cnt]);
                            result1 = accMasterItemsDAOobj.addUpdateMasterCustomItemMapping(requestParam);
                        }
                    }
                        //check for parent master item is mapped to user group
                        String parentValue1 = accMasterItemsService.getUserGroupParentForMap(requestParam);                      
                }
                }
            } else {
                msg = "Sorry, you cannot set child as a parent.";
                result = cntResult;
            }
            isPresent = false;            
                        
            //Sync Cost Centers Created To Eclaim
            if(!isCyclic && (combo!=null && combo.getField() != null) && combo.getField().getIsforeclaim() == 1) {
                try {
                    JSONObject syncObj = new JSONObject();
                    syncObj.put("erpid", combo.getId());
                    syncObj.put("name", combo.getValue());
                    syncObj.put("description", combo.getItemdescription());
                    syncObj.put("eclaimid", combo.getEclaimid());
                    syncArray.put(syncObj);
                } catch (JSONException ex) {
                    Logger.getLogger(accMasterItemsController.class.getName()).log(Level.SEVERE, null, ex);
        }
            }
        }
        
        if (syncArray.length()>0) {
            HashMap<String, Object> params = new HashMap<String, Object>();
            params.put("ids", syncArray);
            params.put("companyid", sessionHandlerImpl.getCompanyid(request));
            syncCostCentersCreatedToEclaim(params);
        }
        if(result!=null){
            return new KwlReturnObject(true, msg, null, result.getEntityList(), 0);
        }else{
            return new KwlReturnObject(false, msg, null, null, 0);
        }
    }

    public void syncCostCentersCreatedToEclaim(HashMap<String, Object> params) {
        try {
            String companyid = (String) params.get("companyid");
            JSONArray syncArray = (JSONArray) params.get("ids");

            //Fetched data from Deskera eClaim
            String action = "19";
            String eclaimURL = this.getServletContext().getInitParameter("eclaimURL");
            JSONObject userData = new JSONObject();
            userData.put("iscommit", true);
            userData.put("remoteapikey", StorageHandler.GetRemoteAPIKey());
            userData.put("companyid", companyid);
            userData.put("callfrom", 3);
            userData.put("isnew", false);
            userData.put("erpcostcenterids", syncArray);
            apiCallHandlerService.callApp(eclaimURL, userData, companyid, action);
        } catch (JSONException ex) {
            Logger.getLogger(accMasterItemsController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }
    }
//    
    public ModelAndView saveMasterPriceDependentItem(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("MI_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        try {
            KwlReturnObject result = saveMasterPriceDependentItem(request);
            if (result.isSuccessFlag()) {
                issuccess = true;
                msg = result.getMsg();
                PriceType priceType = (PriceType) result.getEntityList().get(0);
                jobj.put("id", priceType.getID());
                txnManager.commit(status);
            } else {
                issuccess = true;
                txnManager.commit(status);
                msg = result.getMsg();
            }
        } catch (Exception ex) {
            txnManager.rollback(status);
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
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

       public boolean getCyclic(Iterator<FieldComboData> chieldValues, String parent, boolean isCyclic) {

        while (chieldValues.hasNext()) {
            FieldComboData item = (FieldComboData) chieldValues.next();
            if (item.getId().equals(parent)) {
                isCyclic = true;
                break;
            }
            if (item.getChildren().size() > 0) {
                isCyclic = getCyclic(item.getChildren().iterator(), parent, isCyclic);
            }
        }
        return isCyclic;
    }

    public boolean getCyclicMasterItem(Iterator<MasterItem> chieldValues, String parent, boolean isCyclic) {
        while (chieldValues.hasNext()) {
            MasterItem item = (MasterItem) chieldValues.next();
            if (item.getID().equals(parent)) {
                isCyclic = true;
                break;
            }
            if (item.getChildren().size() > 0) {
                isCyclic = getCyclicMasterItem(item.getChildren().iterator(), parent, isCyclic);
            }
        }
        return isCyclic;
    }
   public ModelAndView deleteMasterItem(HttpServletRequest request, HttpServletResponse response) {
   JSONObject jobj = new JSONObject();
        try {
            JSONObject paramJobj = StringUtil.convertRequestToJsonObject(request);
            String url = this.getServletContext().getInitParameter(Constants.inventoryURL);
            paramJobj.put(Constants.locale, RequestContextUtils.getLocale(request));
            paramJobj.put(Constants.inventoryURL, url);
            jobj = accMasterItemsService.deleteMasterItem(paramJobj);
            jobj.put(Constants.userSessionId, (request.getSession(true)).getAttribute(Constants.userSessionId));// adding user session id to idenntify unique user session
            return new ModelAndView("jsonView", "model", jobj.toString());
        } catch (Exception ex) {
            Logger.getLogger(accMasterItemsController.class.getName()).log(Level.SEVERE, null, ex);

        } finally {
//            if (issuccess && !StringUtil.isNullOrEmpty(channelName)) {
//                ServerEventManager.publish(channelName, jobj.toString(), (ServletContext) this.getServletContext());
//            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

  public ModelAndView deleteLocationItem(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false, isCommitEx = false;
        String msg = "";
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("MI_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        try {
            int no = deleteLocationItem(request);
            issuccess = true;
            msg = messageSource.getMessage("acc.master.del", null, RequestContextUtils.getLocale(request));   //"Master item has been deleted successfully";
            try {
                String name[] = request.getParameterValues("name");
                String groupName = request.getParameter("groupName");
                for (int i = 0; i < name.length; i++) {
                    auditTrailObj.insertAuditLog(AuditAction.MASTER_GROUP, "User " + sessionHandlerImpl.getUserFullName(request) + " has deleted " + groupName + " item " + name[i], request, "0");
                }
                txnManager.commit(status);
            } catch (Exception ex) {
                isCommitEx = true;
                msg = messageSource.getMessage("acc.master.excp1", null, RequestContextUtils.getLocale(request));   //"The Master Item(s) is or had been used in transaction(s). So, it cannot be deleted.";
            }
         } catch (AccountingException acc) {
            txnManager.rollback(status);
            msg = acc.getMessage();
            issuccess = false;
                }catch (ServiceException ex) {
            if (!isCommitEx) {
                txnManager.rollback(status);
            }
            msg = "" + ex.getMessage();
            Logger.getLogger(accMasterItemsController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            txnManager.rollback(status);
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
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

     public int deleteLocationItem(HttpServletRequest request) throws ServiceException, AccountingException, SessionExpiredException {
        String ids[] = request.getParameterValues("ids");
        String locationid = "";
        String companyid = sessionHandlerImpl.getCompanyid(request);
        int numRows = 0;
        try {
            for (int i = 0; i < ids.length; i++) {
                locationid = ids[i];

                KwlReturnObject result = accMasterItemsDAOobj.getdefault_location(locationid, companyid); //Is default location of company
                List list1 = result.getEntityList();
                int count1 = list1.size();

                result = accMasterItemsDAOobj.getProductsusedinlocations(locationid, companyid);  // Is location used in product ?
                List list2 = result.getEntityList();
                int count2 = list2.size();


                result = accMasterItemsDAOobj.getBatches_locations(locationid, companyid); // Is  locationUsed in newproductBatch?
                List list3 = result.getEntityList();
                int count3 = list3.size();

                result = accMasterItemsDAOobj.getSO_locations(locationid, companyid); // Is  location used in consignment Reuest
                List list7 = result.getEntityList();
                int count7 = list7.size();

                result = accMasterItemsDAOobj.getWarehouses_locations(locationid, companyid);  // Is  locationused in Customer Invoice?
                List list4 = result.getEntityList();
                int count4 = list4.size();

                result = accMasterItemsDAOobj.getConsignmentRequest_locations(locationid, companyid); //Is used in Purchase Requisition?
                List list10 = result.getEntityList();
                int count10 = list10.size();
                if (count1 > 0 || count2 > 0 || count3 > 0 || count7 > 0 || count4 > 0 || count10 > 0) {
                    throw new AccountingException(messageSource.getMessage("acc.location.excp3", null, RequestContextUtils.getLocale(request)));   //"Selected record(s) is currently used in the Transaction(s). So it cannot be deleted.");
                }
                accMasterItemsDAOobj.deleteLocationItem(ids[i]);
                numRows++;
            }
        } catch (ServiceException ex) {
            throw new AccountingException(messageSource.getMessage("acc.location.excp3", null, RequestContextUtils.getLocale(request)));
        }
        return numRows;
    }

    public ModelAndView deleteWarehouseItem(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false, isCommitEx = false;
        String msg = "";
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("MI_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        try {
            int no = deleteWarehouseItem(request);
            issuccess = true;
            msg = messageSource.getMessage("acc.master.del", null, RequestContextUtils.getLocale(request));   //"Master item has been deleted successfully";
            try {
                String name[] = request.getParameterValues("name");
                for (int i = 0; i < name.length; i++) {
                    auditTrailObj.insertAuditLog(AuditAction.WAREHOUSE_CHANGED, "User " + sessionHandlerImpl.getUserFullName(request) + " has deleted warehouse item " + name[i], request, "0");
                }
                txnManager.commit(status);
            } catch (Exception ex) {
                isCommitEx = true;
                msg = messageSource.getMessage("acc.master.excp1", null, RequestContextUtils.getLocale(request));   //"The Master Item(s) is or had been used in transaction(s). So, it cannot be deleted.";
            }
        } catch (AccountingException acc) {
            txnManager.rollback(status);
            msg = acc.getMessage();
            issuccess = false;
                } catch (ServiceException ex) {
            if (!isCommitEx) {
                txnManager.rollback(status);
            }
            msg = "" + ex.getMessage();
            Logger.getLogger(accMasterItemsController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            txnManager.rollback(status);
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
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public int deleteWarehouseItem(HttpServletRequest request) throws ServiceException, AccountingException, SessionExpiredException {
        String ids[] = request.getParameterValues("ids");
        String warehouseid = "";
        String companyid = sessionHandlerImpl.getCompanyid(request);
        int numRows = 0;
        try {
            for (int i = 0; i < ids.length; i++) {
                  warehouseid = ids[i];
       
                    KwlReturnObject result = accMasterItemsDAOobj.getdefault_warehouse(warehouseid, companyid); //Is default warehouse of company
                    List list1 = result.getEntityList();
                    int count1 = list1.size();

                    result = accMasterItemsDAOobj.getProductsusedinWarehouses(warehouseid, companyid);  // Is warehouse used in product ?
                    List list2 = result.getEntityList();
                    int count2 = list2.size();


                    result = accMasterItemsDAOobj.getBatches_warehouses(warehouseid, companyid); // Is Used in newproductBatch?
                    List list3 = result.getEntityList();
                    int count3 = list3.size();
                    
                    result = accMasterItemsDAOobj.getInvoice_warehouses(warehouseid, companyid);  // Is used in Customer Invoice?
                    List list4 = result.getEntityList();
                  
                    result = accMasterItemsDAOobj.getSO_warehouses(warehouseid, companyid); // Is used in consignment Reuest
                    List list7=result.getEntityList();
                    int count7=list7.size();
                    
                    result = accMasterItemsDAOobj.getDO_warehouses(warehouseid, companyid); //Is used in  consignmentDelivery Order?
                    List list8=result.getEntityList();
                    int count8=list8.size();
                    
                    result = accMasterItemsDAOobj.getSR_warehouses(warehouseid, companyid); //Is used in consignment Sales return
                    List list9=result.getEntityList();
                    int count9=list9.size();
                    
                    result = accMasterItemsDAOobj.getcustomer_warehouses(warehouseid, companyid); //Is used in Purchase Requisition?
                    List list10=result.getEntityList();
                    int count10=list10.size();

                    if (count1 > 0 || count2 > 0 || count3 > 0 || count7 > 0 || count8 > 0 || count9 > 0 || count10 > 0){
                    	throw new AccountingException(messageSource.getMessage("acc.warehouse.excp3", null, RequestContextUtils.getLocale(request)));   //"Selected record(s) is currently used in the Transaction(s). So it cannot be deleted.");
                    }
                accMasterItemsDAOobj.deleteWarehouseItem(ids[i]);
                numRows++;
            }
        } catch (ServiceException ex) {
            throw new AccountingException(messageSource.getMessage("acc.master.excp1", null, RequestContextUtils.getLocale(request)));
        }
        return numRows;
    }
    
    
    public ModelAndView deleteDepartmentItem(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false, isCommitEx = false;
        String msg = "";
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("MI_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        try {
            int no = deleteDepartmentItem(request);
            issuccess = true;
            msg = messageSource.getMessage("acc.master.del", null, RequestContextUtils.getLocale(request));   //"Master item has been deleted successfully";
            try {
                String name[] = request.getParameterValues("name");
                String groupName = request.getParameter("groupName");
                for (int i = 0; i < name.length; i++) {
                    auditTrailObj.insertAuditLog(AuditAction.MASTER_GROUP, "User " + sessionHandlerImpl.getUserFullName(request) + " has deleted group " + groupName + " item " + name[i], request, "0");
                }
                txnManager.commit(status);
            } catch (Exception ex) {
                isCommitEx = true;
                msg = messageSource.getMessage("acc.master.excp1", null, RequestContextUtils.getLocale(request));   //"The Master Item(s) is or had been used in transaction(s). So, it cannot be deleted.";
            }
        } catch (ServiceException ex) {
            if (!isCommitEx) {
                txnManager.rollback(status);
            }
            msg = "" + ex.getMessage();
            Logger.getLogger(accMasterItemsController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            txnManager.rollback(status);
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
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public int deleteDepartmentItem(HttpServletRequest request) throws ServiceException, AccountingException {
        String ids[] = request.getParameterValues("ids");
        int numRows = 0;
        try {
            for (int i = 0; i < ids.length; i++) {
                accMasterItemsDAOobj.deleteDepartmentItem(ids[i]);
                numRows++;
            }
        } catch (ServiceException ex) {
            throw new AccountingException(messageSource.getMessage("acc.master.excp1", null, RequestContextUtils.getLocale(request)));
        }
        return numRows;
    }

    public ModelAndView getAllInventoryStores(HttpServletRequest request, HttpServletResponse response)
            throws ServletException {
        JSONObject jobj = new JSONObject();
        int totalRows = 0;
        JSONArray storeArray = new JSONArray();
        //Session session=null;
        try {
            String companyid = sessionHandlerImpl.getCompanyid(request);

            String inventoryURL = this.getServletContext().getInitParameter("inventoryURL");
            JSONObject userData = new JSONObject();
            userData.put("iscommit", true);
            userData.put("remoteapikey", StorageHandler.GetRemoteAPIKey());
            userData.put("userid", sessionHandlerImpl.getUserid(request));
            userData.put("companyid", companyid);
            userData.put("isStore", request.getParameter("isStore"));
            boolean isStore = Boolean.parseBoolean(request.getParameter("isStore"));
            //session = HibernateUtil.getCurrentSession();
            String action = "18";
            JSONObject resObj = apiCallHandlerService.callApp(inventoryURL, userData, companyid, action);
            if (!resObj.isNull("success") && resObj.getBoolean("success")) {
                storeArray = resObj.getJSONArray("data");
                String userId = sessionHandlerImpl.getUserid(request);
                KwlReturnObject jeresult = accountingHandlerDAOobj.getObject(User.class.getName(), userId);
                User user = (User) jeresult.getEntityList().get(0);
                                       
                for (int i = 0; i < storeArray.length(); i++) {
                    JSONObject jSONObject = new JSONObject(storeArray.get(i).toString());
                    HashMap requestParam = AccountingManager.getGlobalParams(request);
                    requestParam.put("user", user);
                    if (isStore) {
                        requestParam.put("id", jSONObject.get("storeid"));
                        requestParam.put("name", jSONObject.get("description"));
                        requestParam.put("isdefault", false);
                        accMasterItemsDAOobj.addWarehouseItem(requestParam);
                        auditTrailObj.insertAuditLog(AuditAction.WAREHOUSE_CHANGED, "User " + sessionHandlerImpl.getUserFullName(request) + " has sync warehouse item " + jSONObject.get("description"), request, "0");
                    } else {
                        requestParam.put("id", jSONObject.get("locationid"));
                        requestParam.put("name", jSONObject.get("locationname"));
                        requestParam.put("isdefault", false);
                        accMasterItemsDAOobj.addLocationItem(requestParam);
                        auditTrailObj.insertAuditLog(AuditAction.LOCATION_CHANGED, "User " + sessionHandlerImpl.getUserFullName(request) + " has sync lacation item " + jSONObject.get("locationname"), request, "0");
                    }
                }

            } else {
                jobj.put("success", false);
                jobj.put("count", totalRows);
                //               jobj.put("msg","");
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            Logger.getLogger(kwlCommonTablesController.class.getName()).log(Level.SEVERE, null, e);
        }
//        finally{
//            HibernateUtil.closeSession(session);
//        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public int deleteMasterItem(HttpServletRequest request) throws ServiceException, AccountingException {
        String ids[] = request.getParameterValues("ids");
        int numRows = 0;
        try {
            for (int i = 0; i < ids.length; i++) {
                accMasterItemsDAOobj.daleteMasterItem(ids[i]);
                numRows++;
            }
        } catch (ServiceException ex) {
            throw new AccountingException(messageSource.getMessage("acc.master.excp1", null, RequestContextUtils.getLocale(request)));
        }
        return numRows;
    }

    public ModelAndView deleteMasterItemPrice(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false, isCommitEx = false;
        String msg = "";
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("MI_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        try {
            int no = deleteMasterItemPrice(request);
            issuccess = true;
            msg = messageSource.getMessage("acc.master.del", null, RequestContextUtils.getLocale(request));   //"Master item has been deleted successfully";
            try {
                txnManager.commit(status);
            } catch (Exception ex) {
                isCommitEx = true;
                msg = messageSource.getMessage("acc.master.excp1", null, RequestContextUtils.getLocale(request));   //"The Master Item(s) is or had been used in transaction(s). So, it cannot be deleted.";
            }
        } catch (ServiceException ex) {
            if (!isCommitEx) {
                txnManager.rollback(status);
            }
            msg = "" + ex.getMessage();
            Logger.getLogger(accMasterItemsController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            txnManager.rollback(status);
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
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public int deleteMasterItemPrice(HttpServletRequest request) throws ServiceException, AccountingException {
        String id = request.getParameter("itempriceid");
        int numRows = 0;
        try {

            accMasterItemsDAOobj.deleteMasterItemPrice(id);
        } catch (ServiceException ex) {
            throw new AccountingException(messageSource.getMessage("acc.master.excp1", null, RequestContextUtils.getLocale(request)));
        }
        return numRows;
    }

    public ModelAndView deleteMasterItemPriceFormula(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false, isCommitEx = false;
        String msg = "";
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("MI_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        try {
            int no = deleteMasterItemPriceFormula(request);
            issuccess = true;
            msg = messageSource.getMessage("acc.master.del", null, RequestContextUtils.getLocale(request));   //"Master item has been deleted successfully";
            try {
                txnManager.commit(status);
            } catch (Exception ex) {
                isCommitEx = true;
                msg = messageSource.getMessage("acc.master.excp1", null, RequestContextUtils.getLocale(request));   //"The Master Item(s) is or had been used in transaction(s). So, it cannot be deleted.";
            }
        } catch (ServiceException ex) {
            if (!isCommitEx) {
                txnManager.rollback(status);
            }
            msg = "" + ex.getMessage();
            Logger.getLogger(accMasterItemsController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            txnManager.rollback(status);
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
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public int deleteMasterItemPriceFormula(HttpServletRequest request) throws ServiceException, AccountingException {
        String id = request.getParameter("itempriceid");
        int numRows = 0;
        try {

            accMasterItemsDAOobj.deleteMasterItemPriceFormula(id);
        } catch (ServiceException ex) {
            throw new AccountingException(messageSource.getMessage("acc.master.excp1", null, RequestContextUtils.getLocale(request)));
        }
        return numRows;
    }

    public ModelAndView getMasterItems(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        try {
            jobj = getMasterItems(request);
            issuccess = true;
        } catch (SessionExpiredException ex) {
            msg = ex.getMessage();
            Logger.getLogger(accMasterItemsController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ServiceException ex) {
            msg = ex.getMessage();
            Logger.getLogger(accMasterItemsController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            msg = "accMasterItemsController.getMasterItems : " + ex.getMessage();
            Logger.getLogger(accMasterItemsController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accMasterItemsController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public ModelAndView getLocationItems(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        try {
            jobj = getLocationItems(request);
            issuccess = true;
        } catch (SessionExpiredException ex) {
            msg = ex.getMessage();
            Logger.getLogger(accMasterItemsController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ServiceException ex) {
            msg = ex.getMessage();
            Logger.getLogger(accMasterItemsController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            msg = "accMasterItemsController.getLocationItems : " + ex.getMessage();
            Logger.getLogger(accMasterItemsController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accMasterItemsController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    public ModelAndView getLocationItemsFromStore(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        try {
            jobj = getLocationItemsFromStore(request);
            issuccess = true;
        } catch (SessionExpiredException ex) {
            msg = ex.getMessage();
            Logger.getLogger(accMasterItemsController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ServiceException ex) {
            msg = ex.getMessage();
            Logger.getLogger(accMasterItemsController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            msg = "accMasterItemsController.getLocationItems : " + ex.getMessage();
            Logger.getLogger(accMasterItemsController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accMasterItemsController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    
    public JSONObject getLocationItemsFromStore(HttpServletRequest request) throws ServiceException, SessionExpiredException {
        JSONObject jobj = new JSONObject();
         JSONArray jArr = new JSONArray();
        try {
            String storeid=request.getParameter("storeid");
            Boolean isActive = Boolean.parseBoolean(request.getParameter("isActive"));
            String companyId = sessionHandlerImpl.getCompanyid(request);
            KwlReturnObject result = accMasterItemsDAOobj.getLocationsFromStore(storeid,companyId);
            List list = result.getEntityList();
            Iterator itr = list.iterator();
            while (itr.hasNext()) {

                String locationId = (String) itr.next();
                KwlReturnObject locresult = accountingHandlerDAOobj.getObject(InventoryLocation.class.getName(), locationId);
                InventoryLocation inventoryLocation = (InventoryLocation) locresult.getEntityList().get(0);
                //ERP-40021 : isActive check is added to to send only Active locations from the store.
                if (isActive) {
                    KwlReturnObject locres = accountingHandlerDAOobj.getObject(Location.class.getName(), locationId);
                    Location location = (Location) locres.getEntityList().get(0);
                    if (location.isActive()) {
                        if (inventoryLocation != null) {
                            JSONObject obj = new JSONObject();
                            obj.put("id", inventoryLocation.getId());
                            obj.put("name", inventoryLocation.getName());
                            obj.put("parentid", inventoryLocation.getParent() != null ? inventoryLocation.getParent().getId() : "");
                            obj.put("parentid", inventoryLocation.getParent() != null ? inventoryLocation.getParent().getName() : "");
                            obj.put("isdefault", inventoryLocation.isIsdefault());
                            jArr.put(obj);
                        }
                    }
                } else {
                    if (inventoryLocation != null) {
                        JSONObject obj = new JSONObject();
                        obj.put("id", inventoryLocation.getId());
                        obj.put("name", inventoryLocation.getName());
                        obj.put("parentid", inventoryLocation.getParent() != null ? inventoryLocation.getParent().getId() : "");
                        obj.put("parentid", inventoryLocation.getParent() != null ? inventoryLocation.getParent().getName() : "");
                        obj.put("isdefault", inventoryLocation.isIsdefault());
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
    public ModelAndView checkDuplicateSerialforProduct(HttpServletRequest request, HttpServletResponse response) {
        boolean issuccess = false;
        String msg = "";
        JSONObject jobj = new JSONObject();
        try {
            String batchid=request.getParameter("batchid");
            String productid=request.getParameter("productid");
            String serialName=request.getParameter("serialname");
            String companyId = sessionHandlerImpl.getCompanyid(request);
            KwlReturnObject result = accMasterItemsDAOobj.checkDuplicateSerialforProduct(productid,batchid,serialName,companyId);
            List list = result.getEntityList();
            int duplicateCount=result.getRecordTotalCount();
            jobj.put("duplicateCount", duplicateCount);
            issuccess=true;
         } catch (SessionExpiredException ex) {
            msg = ex.getMessage();
            Logger.getLogger(accMasterItemsController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } catch (ServiceException ex) {
            msg = ex.getMessage();
            Logger.getLogger(accMasterItemsController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } catch (Exception ex) {
            msg = "accMasterItemsController.getDuplicateSerials : " + ex.getMessage();
            Logger.getLogger(accMasterItemsController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accMasterItemsController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    public ModelAndView getDepartments(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        try {
            jobj = getDepartments(request);
            issuccess = true;
        } catch (SessionExpiredException ex) {
            msg = ex.getMessage();
            Logger.getLogger(accMasterItemsController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } catch (ServiceException ex) {
            msg = ex.getMessage();
            Logger.getLogger(accMasterItemsController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } catch (Exception ex) {
            msg = "accMasterItemsController.getDepartments : " + ex.getMessage();
            Logger.getLogger(accMasterItemsController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accMasterItemsController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public ModelAndView getBatches(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        try {
            jobj = accMasterItemsService.getBatches(request);
            issuccess = true;
        } catch (SessionExpiredException ex) {
            msg = ex.getMessage();
            Logger.getLogger(accMasterItemsController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ServiceException ex) {
            msg = ex.getMessage();
            Logger.getLogger(accMasterItemsController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            msg = "accMasterItemsController.getBatches : " + ex.getMessage();
            Logger.getLogger(accMasterItemsController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accMasterItemsController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    public ModelAndView getNewBatches(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        try {
            JSONObject paramJobj = StringUtil.convertRequestToJsonObject(request);
            jobj = accMasterItemsService.getNewBatches(paramJobj);
            issuccess = true;
        } catch (SessionExpiredException ex) {
            msg = ex.getMessage();
            Logger.getLogger(accMasterItemsController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ServiceException ex) {
            msg = ex.getMessage();
            Logger.getLogger(accMasterItemsController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            msg = "accMasterItemsController.getNewBatches : " + ex.getMessage();
            Logger.getLogger(accMasterItemsController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accMasterItemsController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    
    public ModelAndView getSerials(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        try {
            jobj = getSerials(request);
            issuccess = true;
        } catch (SessionExpiredException ex) {
            msg = ex.getMessage();
            Logger.getLogger(accMasterItemsController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ServiceException ex) {
            msg = ex.getMessage();
            Logger.getLogger(accMasterItemsController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            msg = "accMasterItemsController.getSerials : " + ex.getMessage();
            Logger.getLogger(accMasterItemsController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accMasterItemsController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }   
    public ModelAndView getNewSerials(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        try {
            JSONObject paramJobj = StringUtil.convertRequestToJsonObject(request);
            jobj = accMasterItemsService.getNewSerials(paramJobj);
            issuccess = true;
        } catch (SessionExpiredException ex) {
            msg = ex.getMessage();
            Logger.getLogger(accMasterItemsController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ServiceException ex) {
            msg = ex.getMessage();
            Logger.getLogger(accMasterItemsController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            msg = "accMasterItemsController.getNewSerials : " + ex.getMessage();
            Logger.getLogger(accMasterItemsController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accMasterItemsController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public ModelAndView getWarehouseItems(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        try {
            jobj = getWarehouseItems(request);
            issuccess = true;
        } catch (SessionExpiredException ex) {
            msg = ex.getMessage();
            Logger.getLogger(accMasterItemsController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ServiceException ex) {
            msg = ex.getMessage();
            Logger.getLogger(accMasterItemsController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            msg = "accMasterItemsController.getLocationItems : " + ex.getMessage();
            Logger.getLogger(accMasterItemsController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accMasterItemsController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    
    public JSONObject getMasterItems(HttpServletRequest request) throws ServiceException, SessionExpiredException, UnsupportedEncodingException {
        JSONObject jobj = new JSONObject();
        int totalCount=0;
        try {
            String companyId = sessionHandlerImpl.getCompanyid(request);
            KwlReturnObject extraCompanyPrefObjResult = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), companyId);
            ExtraCompanyPreferences extraCompanyPref = (ExtraCompanyPreferences) extraCompanyPrefObjResult.getEntityList().get(0);
            JSONArray jArr = new JSONArray();
//            session = HibernateUtil.getCurrentSession();
            HashMap<String, Object> filterRequestParams = new HashMap<String, Object>();
            ArrayList filter_names = new ArrayList(), filter_params = new ArrayList(), order_by = new ArrayList(), order_type = new ArrayList();
            String grID = request.getParameter("groupid");
            String parentmasteritem = "";
            if(!StringUtil.isNullOrEmpty(request.getParameter("parentmasteritem"))){
                parentmasteritem = request.getParameter("parentmasteritem");
            }
            boolean isIBGPaidTo = StringUtil.getBoolean(request.getParameter("isIBGPaidTo"));
            int bankType = !StringUtil.isNullOrEmpty(request.getParameter("bankType"))?Integer.parseInt(request.getParameter("bankType").toString()):0;
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
                if (request.getParameter("ss") != null && !StringUtil.isNullOrEmpty(request.getParameter("ss"))) {
                    String ss = (String) request.getParameter("ss");
                    filterRequestParams.put("ss", ss);
                    filterRequestParams.put("ss_names", new String[]{"value"});
                } else if (request.getParameter("query") != null && !StringUtil.isNullOrEmpty(request.getParameter("query"))) {
                    String ss = (String) request.getParameter("query");
                    filterRequestParams.put("ss", ss);
                    filterRequestParams.put("ss_names", new String[]{"value"});
                }
                 if ((request.getParameter("start") != null) && (request.getParameter("limit") != null)) {
                    filterRequestParams.put("start", request.getParameter("start"));
                    filterRequestParams.put("limit", request.getParameter("limit"));
                }
                KwlReturnObject result = accMasterItemsDAOobj.getMasterItemsHire(filterRequestParams);
                totalCount=result.getRecordTotalCount();
                List<Object[]> list = result.getEntityList();
                for(Object[] row:list) {
                    MasterItem fieldComboData = (MasterItem) row[0];
                    JSONObject obj = new JSONObject();
                    obj.put("id", fieldComboData.getID());
                    obj.put("name", fieldComboData.getValue());
                    obj.put("isDefaultToPOS", fieldComboData.isDefaultToPOS());
                    MasterItem parentItem = (MasterItem) row[3];
                    if (parentItem != null) {
                        obj.put("parentid", parentItem.getID());
                        obj.put("parentname", parentItem.getValue());
                    }
                    obj.put("level", row[1]);
                    obj.put("leaf", row[2]);
                    obj.put("variancePercentage", fieldComboData.getVariancePercentage());
                    obj.put("industryCodeId", fieldComboData.getIndustryCodeId());
                    jArr.put(obj);
                }
            }else if(grID.equals("60")){
                filterRequestParams=new HashMap<String, Object>();
                filterRequestParams.put(Constants.companyKey, companyId);
                 if ((request.getParameter("start") != null) && (request.getParameter("limit") != null)) {
                    filterRequestParams.put("start", request.getParameter("start"));
                    filterRequestParams.put("limit", request.getParameter("limit"));
                }
                KwlReturnObject result = accMasterItemsDAOobj.getMasterItemFromLandingCostCategory(filterRequestParams);
                List<LandingCostCategory> list = result.getEntityList();
                totalCount=result.getRecordTotalCount();
                for (LandingCostCategory landingCostCategory:list) {
                    JSONObject obj = new JSONObject();
                    obj.put("id", landingCostCategory.getId());
                    obj.put("landingcostcategory", landingCostCategory.getLccName());
                    obj.put("landingcostallocationtype", landingCostCategory.getLcallocationid());
                    jArr.put(obj);
                }
            } else {
                if (grID.equals("15")) {// if salesperson combo and salesperson and agent flow is on for owners restriction feature.
                    boolean onlyLoggedInuserSalepersons = StringUtil.getBoolean(request.getParameter("onlyloggedinusersalespersons"));
                    if (onlyLoggedInuserSalepersons) {
                        ExtraCompanyPreferences extraPref = (ExtraCompanyPreferences) kwlCommonTablesDAOObj.getClassObject("com.krawler.common.admin.ExtraCompanyPreferences", sessionHandlerImpl.getCompanyid(request));
                        if (extraPref != null && extraPref.isEnablesalespersonAgentFlow()) {
                            int permCode = sessionHandlerImpl.getPerms(request, Constants.CUSTOMER_PERMCODE);
                            if (!((permCode & Constants.CUSTOMER_VIEWALL_PERMCODE) == Constants.CUSTOMER_VIEWALL_PERMCODE)) {
                                /*
                                 * when (permCode & Constants.CUSTOMER_VIEWALL_PERMCODE) == Constants.CUSTOMER_VIEWALL_PERMCODE is true then user has permission to view all customers documents,so at that time there is need to filter record according to user&salesperson. 
                                 */
                                
                                Map<String,Object> salesPersonParams=new HashMap<>();
                                salesPersonParams.put("userid", sessionHandlerImpl.getUserid(request));
                                salesPersonParams.put("companyid", sessionHandlerImpl.getCompanyid(request));
                                salesPersonParams.put("grID", grID);
                                KwlReturnObject masterItemByUserList = accountingHandlerDAOobj.getMasterItemByUserID(salesPersonParams);
                                if (masterItemByUserList.getEntityList().size() > 0) {//if user has salesperson then show only his salesperson in salesperson combo in sales by salesperson report else show all salespersons in combo
                                    filter_names.add("user.userID");
                                    filter_params.add(sessionHandlerImpl.getUserid(request));
                                }
//                              
                            }
                        }
                    }
                } else if (extraCompanyPref.isIsNewGST() && grID.equals("37")) {
                    /**
                     * For New GST company 
                     */
                    filter_names.add("code");
                    filter_params.add("7");
                }
                filter_names.add("masterGroup.ID");
                filter_params.add(grID);
                filter_names.add("company.companyID");
                filter_params.add(sessionHandlerImpl.getCompanyid(request));
                if(extraCompanyPref != null && extraCompanyPref.isActivateMRPModule() && grID.equals(Constants.MASTERCONFIG_QUALITY_PARAMETER) && !StringUtil.isNullOrEmpty(parentmasteritem)){
                    filter_names.add("parent.ID");
                    filter_params.add(parentmasteritem);
                }
                if (isIBGPaidTo) {
                    filter_names.add("ibgActivated");
                    filter_params.add(isIBGPaidTo);
                    if(bankType == Constants.DBS_BANK_Type){
                        filter_names.add("INID");
                        filter_params.add("Select masterItem.id from IBGReceivingBankDetails cimb");
                    }else if(bankType == Constants.CIMB_BANK_Type){
                        filter_names.add("INID");
                        filter_params.add("Select masterItem.id from CIMBReceivingDetails ibg");
                    }    
                }
                if ((request.getParameter("start") != null) && (request.getParameter("limit") != null)) {
                    filterRequestParams.put("start", request.getParameter("start"));
                    filterRequestParams.put("limit", request.getParameter("limit"));
                }
                
                if (request.getParameter("ss") != null && !StringUtil.isNullOrEmpty(request.getParameter("ss"))) {
                    String ss = (String) request.getParameter("ss");
                    filterRequestParams.put("ss", ss);
                    filterRequestParams.put("ss_names",  new String[]{"value"});
                } else if (request.getParameter("query") != null && !StringUtil.isNullOrEmpty(request.getParameter("query"))) {
                    String ss = (String) request.getParameter("query");
                    filterRequestParams.put("ss", ss);
                    filterRequestParams.put("ss_names",  new String[]{"value"});
                }
                order_by.add("value");
                order_type.add("asc");
                filterRequestParams.put("filter_names", filter_names);
                filterRequestParams.put("filter_params", filter_params);
                filterRequestParams.put("order_by", order_by);
                filterRequestParams.put("order_type", order_type);
                KwlReturnObject result = accMasterItemsDAOobj.getMasterItems(filterRequestParams);

                List list = result.getEntityList();
                totalCount=result.getRecordTotalCount();
                MasterItem item;
                List list2;
                Iterator itr;
                String customerid = request.getParameter("customerid");
                String vendorid = request.getParameter("vendorid");
                
                if (!StringUtil.isNullOrEmpty(customerid)) { //if customer is selected then filterout masteritems for particular that customer
                    KwlReturnObject res = accMasterItemsDAOobj.getMasterItemFromCustomerID(customerid);
                    list2 = res.getEntityList();
                    totalCount=result.getRecordTotalCount();
                    itr = list2.iterator();
                    if (!itr.hasNext()) { // if customer do not have available masteritems, then show all master items.
                        itr = list.iterator();
                    }
                } else if (!StringUtil.isNullOrEmpty(vendorid)) { //if customer is selected then filterout masteritems for particular that customer
                    KwlReturnObject res = accMasterItemsDAOobj.getMasterItemFromVendorID(vendorid);
                    list2 = res.getEntityList();
                    totalCount=result.getRecordTotalCount();
                    itr = list2.iterator();
                    if (!itr.hasNext()) { // if customer do not have available masteritems, then show all master items.
                        itr = list.iterator();
                    }
                } else { // if customer isnot selected ,then show all master items.
                    itr = list.iterator();
                }
                while (itr.hasNext()) {
                    item = (MasterItem) itr.next();
                    
                    // If loading master items for Stock Adjustment Group then do not load 'Wastage' if Wastage Calculation is not activated in system preferences.
                    if (extraCompanyPref != null && !extraCompanyPref.isActivateWastageCalculation() && grID.equals("31") && item.getDefaultMasterItem() != null && item.getDefaultMasterItem().getID().equalsIgnoreCase(Constants.WASTAGE_ID)) {
                        continue;
                    }
                    
                    JSONObject obj = new JSONObject();
                    obj.put("id", item.getID());
                    JSONArray dataArrayForDBS = new JSONArray();
                    JSONArray dataArrayForCIMB = new JSONArray();
                    if (item.isIbgActivated()) {
                        request.setAttribute("masterItemId", item.getID());
                        dataArrayForDBS = getIBGReceivingBankDetails(request);
                        if(dataArrayForDBS.length()!=0){
                            obj.put("DBSbank", true);
                        }
                        dataArrayForCIMB = getCIMBReceivingBankDetails(request);
                        if(dataArrayForCIMB.length()!=0){
                            obj.put("CIMBbank", true);
                        }
                        for(int i=0;i<dataArrayForCIMB.length();i++){
                            dataArrayForDBS.put(dataArrayForCIMB.getJSONObject(i));
                        }
                        obj.put("ibgReceivingDetails", dataArrayForDBS);
                    }
                    obj.put("salespersoncode", item.getCode());
                    obj.put("salesPersonContactNumber", item.getContactNumber());
                    obj.put("salesPersonAddress", item.getAddress());
                    obj.put("salesPersonDesignation", item.getDesignation());
                    obj.put("activated", item.isActivated());
                    obj.put("hasAccess", item.isActivated());
                    obj.put("isIbgActivItematedForPaidTo", item.isIbgActivated());
                    obj.put("name",!StringUtil.isNullOrEmpty(item.getValue()) ? StringUtil.DecodeText(item.getValue()):"");//Allow chinese characters to save ERP-24608
                    if (grID.equals("33")) {
                        obj.put("name", item.getCode() +" - "+ item.getValue());
                    }
                    if(extraCompanyPref.isActivateMRPModule()){
                        obj.put("parentid", item.getParent()!=null ? item.getParent().getID() : "");
                    }else{
                        obj.put("parentid", "");
                    }
                    obj.put("leaf", true);
                    obj.put("level", 0);
                    obj.put("groupid", grID);//For Identifing group Value
                    obj.put("emailid", (item.getEmailID() != null) ? item.getEmailID() : "");
                    obj.put("userid", (item.getUser() != null) ? item.getUser().getUserID() : "");
                    obj.put("typeid", item.getCustVendCategoryType());
                    obj.put("username", (item.getUser() != null) ? StringUtil.getFullName(item.getUser()) : "");
                    obj.put("driverID", (item.getDriver() != null) ? item.getDriver().getID() : "");
                    obj.put("isDefaultToPOS", item.isDefaultToPOS());
                    obj.put("defaultMasterItem", (item.getDefaultMasterItem() != null) ? item.getDefaultMasterItem().getID() : "");
                    obj.put("natureofpaymentdesc",item.getValue());
                    obj.put("natureofpaymentsection",item.getCode());
                    obj.put("typeofdeducteetype", item.getCode());
                    obj.put("vatcommoditycode", item.getVatcommoditycode());
                    obj.put("vatscheduleno", item.getVatscheduleno());
                    obj.put("vatscheduleserialno", item.getVatscheduleserialno());
                    obj.put("vatnotes", item.getVatnotes());
                    obj.put("code", item.getCode());
                    obj.put("accid", (item.getAccID() != null) ? item.getAccID() : "");
                    obj.put("landingcostcategory",item.getLccategoryid()!=null?item.getLccategoryid().getId():"");
                    obj.put("landingcostallocationtype", item.getLcallocationid()!=null?item.getLcallocationid().getLCAllocationId():"");
                    obj.put("BICCode", item.getBICCode()!=null?item.getBICCode():"");
                    obj.put("bankCode", item.getBankCode()!=null?item.getBankCode():"");
                    obj.put("branchCode", item.getBranchCode()!=null?item.getBranchCode():"");
                    obj.put("isAppendBranchCode", item.isIsAppendBranchCode());
                    jArr.put(obj);
                }
            }
            jobj.put("data", jArr);
            jobj.put("totalCount", totalCount);
        } catch (JSONException e) {
            throw ServiceException.FAILURE(e.getMessage(), e);
        } 
//        finally {
//            HibernateUtil.closeSession(session);
//        }
        return jobj;
    }
 
   public JSONArray getIBGReceivingBankDetails(HttpServletRequest request) throws SessionExpiredException, ServiceException {
        JSONArray returnArray = new JSONArray();
        try {
            HashMap<String, Object> requestParams = getIBGReceivingBankDetailsRequestParamsMap(request);

            KwlReturnObject returnObject = accMasterItemsDAOobj.getIBGReceivingBankDetails(requestParams);

            if (returnObject != null && !returnObject.getEntityList().isEmpty()) {
                returnArray = getIBGReceivingBankDetails(returnObject.getEntityList());
            }

        } catch (ServiceException ex) {
            Logger.getLogger(accMasterItemsController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            throw ServiceException.FAILURE("getIBGReceivingBankDetailsForVendor : " + ex.getMessage(), ex);
        } catch (JSONException ex) {
            Logger.getLogger(accMasterItemsController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            throw ServiceException.FAILURE("getIBGReceivingBankDetailsForVendor : " + ex.getMessage(), ex);
        }
        return returnArray;
    }
  

    public JSONObject getSerials(HttpServletRequest request) throws ServiceException, SessionExpiredException {
        JSONObject jobj = new JSONObject();
        try {
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            boolean isDO = false;
            boolean isEdit = false;
            boolean copyTrans = false;
            String billid = "";
            String batchId = "";
            int transactionid = 0;
            String companyId = sessionHandlerImpl.getCompanyid(request);

            if (!StringUtil.isNullOrEmpty(companyId)) {
                requestParams.put("companyid", companyId);
            }
            if (!StringUtil.isNullOrEmpty("productid")) {
                requestParams.put("productid", request.getParameter("productid"));
            }
            if (!StringUtil.isNullOrEmpty(request.getParameter("duplicatecheck"))) {
                requestParams.put("duplicatecheck", Boolean.parseBoolean(request.getParameter("duplicatecheck")));
            }
            if (!StringUtil.isNullOrEmpty(request.getParameter("batch"))) {
                requestParams.put("batch", request.getParameter("batch"));
                batchId =request.getParameter("batch");
            }
            if (!StringUtil.isNullOrEmpty(request.getParameter("transType"))) {
                requestParams.put("transType", request.getParameter("transType"));
            }
            if (!StringUtil.isNullOrEmpty(request.getParameter("transactionid"))) {
                transactionid = Integer.parseInt((String) request.getParameter("transactionid"));
                requestParams.put("transactionid", request.getParameter("transactionid"));
            }

            if (!StringUtil.isNullOrEmpty(request.getParameter("isDO"))) {
                isDO = Boolean.parseBoolean(request.getParameter("isDO"));
            }
            if (!StringUtil.isNullOrEmpty(request.getParameter("isEdit"))) {
                isEdit = Boolean.parseBoolean(request.getParameter("isEdit"));
            }
            if (!StringUtil.isNullOrEmpty(request.getParameter("copyTrans"))) {
                copyTrans = Boolean.parseBoolean(request.getParameter("copyTrans"));
            }
            if (!StringUtil.isNullOrEmpty(request.getParameter("billid"))) {
                billid = request.getParameter("billid");
            }
            KwlReturnObject result = accMasterItemsDAOobj.getSerials(requestParams);


            List list = result.getEntityList();
            Iterator itr = list.iterator();
            JSONArray jArr = new JSONArray();
            while (itr.hasNext()) {
                Object[] oj = (Object[]) itr.next();
                String batchid = oj[0].toString();
                KwlReturnObject batchs = accountingHandlerDAOobj.getObject(BatchSerial.class.getName(), batchid);
                List<BatchSerial> prd = batchs.getEntityList();
                JSONObject obj = new JSONObject();
//                if (isDO) {
//                    HashMap<String, Object> filterRequestParams = new HashMap<String, Object>();
//                    ArrayList filter_names = new ArrayList(), filter_params = new ArrayList();
//                    filter_names.add("maptoserial.id");
//                    filter_params.add(prd.get(0).getId());
//                    filterRequestParams.put("filter_names", filter_names);
//                    filterRequestParams.put("filter_params", filter_params);
//                    KwlReturnObject resultSerial = accMasterItemsDAOobj.getSerialItems(filterRequestParams);
//                    if (resultSerial != null && resultSerial.getEntityList().size() > 0) {
//                        continue;
//                    }
//                }
                if (transactionid == 3) {
                    HashMap<String, Object> filterRequestParams = new HashMap<String, Object>();
                    ArrayList filter_names = new ArrayList(), filter_params = new ArrayList();
                    filter_names.add("mapserial.id");
                    filter_params.add(prd.get(0).getId());
                    filterRequestParams.put("filter_names", filter_names);
                    filterRequestParams.put("filter_params", filter_params);
                    KwlReturnObject resultSerialSR = accMasterItemsDAOobj.getSerialItems(filterRequestParams);
                    if (resultSerialSR != null && resultSerialSR.getEntityList().size() > 0) {
                        continue;
                    }
                }
                obj.put("id", prd.get(0).getId());
                obj.put("serialno", prd.get(0).getName());
                obj.put("serialnoid", prd.get(0).getId());
                obj.put("expstart", prd.get(0).getExpfromdate() != null ? prd.get(0).getExpfromdate() : "");
                obj.put("expend", prd.get(0).getExptodate() != null ? prd.get(0).getExptodate() : "");
                obj.put("purchaseserialid", prd.get(0).getId());
                obj.put("purchasebatchid", prd.get(0).getBatch().getId());
                jArr.put(obj);

            }
            if (isEdit && !copyTrans && !StringUtil.isNullOrEmpty(billid) && !StringUtil.isNullOrEmpty(request.getParameter("transactionid"))) {
                HashMap<String, Object> requestParamsSerial = new HashMap<String, Object>();
                if (!StringUtil.isNullOrEmpty(billid)) {
                    requestParamsSerial.put("billid", billid);
                }
                if (!StringUtil.isNullOrEmpty(request.getParameter("transactionid"))) {
                    requestParamsSerial.put("transactionid", request.getParameter("transactionid"));
                }
                KwlReturnObject resultSerial = accMasterItemsDAOobj.getSerialsinEdit(requestParamsSerial);
                List listSerial = resultSerial.getEntityList();
                Iterator itrSerial = listSerial.iterator();
                while (itrSerial.hasNext()) {
                    Object[] oj = (Object[]) itrSerial.next();
                    String batchid = oj[0].toString();
                    String purchasebatchid="";
                    KwlReturnObject batchs = accountingHandlerDAOobj.getObject(BatchSerial.class.getName(), batchid);
                    List<BatchSerial> prd = batchs.getEntityList();
                    JSONObject ob = new JSONObject();
                    ob.put("id", prd.get(0).getId());
                    ob.put("serialno", prd.get(0).getName());
                    ob.put("serialnoid", prd.get(0).getId());
                    ob.put("expstart", prd.get(0).getExpfromdate() != null ? prd.get(0).getExpfromdate() : "");
                    ob.put("expend", prd.get(0).getExptodate() != null ? prd.get(0).getExptodate() : "");
                    ob.put("purchaseserialid", prd.get(0).getId());
                    ob.put("purchasebatchid", prd.get(0).getBatch().getId());
                    purchasebatchid=prd.get(0).getBatch().getId();
                    if (!StringUtil.isNullOrEmpty(batchId) && !StringUtil.isNullOrEmpty(purchasebatchid)) {
                        if (batchId.equals(purchasebatchid)) {  //current batchid is equal to previous batch in edit case then only put the record 
                            jArr.put(ob);
                        }
                    }
                }
            }
            jobj.put("data", jArr);
        } catch (JSONException e) {
            throw ServiceException.FAILURE(e.getMessage(), e);
        }
        return jobj;
    }
   

    public JSONObject getLocationItems(HttpServletRequest request) throws ServiceException, SessionExpiredException {
        JSONObject jobj = new JSONObject();
        try {
            HashMap<String, Object> filterRequestParams = new HashMap<String, Object>();
            ArrayList filter_names = new ArrayList(), filter_params = new ArrayList(), order_by = new ArrayList(), order_type = new ArrayList();
            filter_names.add("company.companyID");
            filter_params.add(sessionHandlerImpl.getCompanyid(request));
            order_by.add("name");
            order_type.add("asc");
            filterRequestParams.put("filter_names", filter_names);
            filterRequestParams.put("filter_params", filter_params);
            filterRequestParams.put("order_by", order_by);
            filterRequestParams.put("order_type", order_type);
            KwlReturnObject result = accMasterItemsDAOobj.getLocationItems(filterRequestParams);

            List list = result.getEntityList();
            Iterator itr = list.iterator();
            JSONArray jArr = new JSONArray();
            while (itr.hasNext()) {
                InventoryLocation inventoryLocation = (InventoryLocation) itr.next();
                JSONObject obj = new JSONObject();
                obj.put("id", inventoryLocation.getId());
                obj.put("name", inventoryLocation.getName());
                obj.put("parentid", inventoryLocation.getParentId() != null ? inventoryLocation.getParentId() : "");
//                obj.put("parentid", inventoryLocation.getParent() != null ? inventoryLocation.getParent().getName() : "");
                obj.put("isdefault", inventoryLocation.isIsdefault());
                jArr.put(obj);
            }
            jobj.put("data", jArr);
        } catch (JSONException e) {
            throw ServiceException.FAILURE(e.getMessage(), e);
        }
        return jobj;
    }

    public JSONObject getDepartments(HttpServletRequest request) throws ServiceException, SessionExpiredException {
        JSONObject jobj = new JSONObject();
        try {
            String ss= request.getParameter("ss");
            HashMap<String, Object> filterRequestParams = new HashMap<String, Object>();
            ArrayList filter_names = new ArrayList(), filter_params = new ArrayList(), order_by = new ArrayList(), order_type = new ArrayList();
            filter_names.add("company.companyID");
            filter_params.add(sessionHandlerImpl.getCompanyid(request));
            if(!StringUtil.isNullOrEmpty(ss)){
                filterRequestParams.put("ss",ss);
                filterRequestParams.put("ss_names",  new String[]{"name"});
            }
            order_by.add("name");
            order_type.add("asc");
            filterRequestParams.put("filter_names", filter_names);
            filterRequestParams.put("filter_params", filter_params);
            filterRequestParams.put("order_by", order_by);
            filterRequestParams.put("order_type", order_type);
            KwlReturnObject result = accMasterItemsDAOobj.getDepartments(filterRequestParams);

            List list = result.getEntityList();
            Iterator itr = list.iterator();
            JSONArray jArr = new JSONArray();
            while (itr.hasNext()) {
                Department department = (Department) itr.next();
                JSONObject obj = new JSONObject();
                obj.put("id", department.getId());
                obj.put("name", department.getName());
                jArr.put(obj);
            }
            jobj.put("data", jArr);
        } catch (JSONException e) {
            throw ServiceException.FAILURE(e.getMessage(), e);
        }
        return jobj;
    }

    public JSONObject getWarehouseItems(HttpServletRequest request) throws ServiceException, SessionExpiredException {
        JSONObject jobj = new JSONObject();
        try {
            String userId = sessionHandlerImpl.getUserid(request);
            KwlReturnObject jeresult = accountingHandlerDAOobj.getObject(User.class.getName(), userId);
            User loginUser = (User) jeresult.getEntityList().get(0);
            String searchString = request.getParameter("ss");
            String companyid = sessionHandlerImpl.getCompanyid(request);
            boolean isForCustomer = request.getParameter("isForCustomer") != null ? Boolean.parseBoolean(request.getParameter("isForCustomer")) : false;
            HashMap<String, Object> filterRequestParams = new HashMap<String, Object>();
            ArrayList filter_names = new ArrayList(), filter_params = new ArrayList(), order_by = new ArrayList(), order_type = new ArrayList();
            ExtraCompanyPreferences pref = (ExtraCompanyPreferences) kwlCommonTablesDAOObj.getClassObject(ExtraCompanyPreferences.class.getName(), companyid);
            filter_names.add("s.company.companyID");
            filter_params.add(companyid);
             if (!StringUtil.isNullOrEmpty(request.getParameter("customerid"))) {
                filter_names.add("customer");
                filter_params.add(request.getParameter("customerid"));
            }
            if (!StringUtil.isNullOrEmpty(request.getParameter("movementtypeid"))) {
                filter_names.add("m.id");
                filter_params.add(request.getParameter("movementtypeid"));
            }
//            if (!StringUtil.isNullOrEmpty(request.getParameter("isForCustomer"))) {
                filter_names.add("isForCustomer");
                filter_params.add(isForCustomer);
//            }
            filter_names.add("s.active");   
            filter_params.add(true);

            boolean includePickandPackStore=false;
            if (!StringUtil.isNullOrEmpty(request.getParameter("includePickandPackStore"))) {
            includePickandPackStore=Boolean.parseBoolean(request.getParameter("includePickandPackStore"));
            }
            if (!includePickandPackStore) {
                if (pref.isPickpackship()) {
                    filter_names.add("!s.id");
                    filter_params.add(pref.getPackingstore());
                }
            }
            boolean includeQAAndRepairStore = false;
            if (!StringUtil.isNullOrEmpty(request.getParameter("includeQAAndRepairStore"))) {
                includeQAAndRepairStore = Boolean.parseBoolean(request.getParameter("includeQAAndRepairStore"));
            }
            /**
             * ERM-691 QA approval flow flags to display only transaction stores and exclude Repair/QA/Scrap Stores.
             */
            boolean isRepairStoresOnly = false;
            boolean isScrapStoreOnly = false;
            if (!StringUtil.isNullOrEmpty(request.getParameter("isRepairStoreOnly"))) {
                isRepairStoresOnly = Boolean.parseBoolean(request.getParameter("isRepairStoreOnly"));
            }
            if (!StringUtil.isNullOrEmpty(request.getParameter("isScrapstoreonly"))) {
                isScrapStoreOnly = Boolean.parseBoolean(request.getParameter("isScrapstoreonly"));
            }
            if (!includeQAAndRepairStore || ((isRepairStoresOnly || isScrapStoreOnly) && pref.isActivateQAApprovalFlow())) {  //block for excluding QA / Scrap / Repair stores
                KwlReturnObject companyResult = accountingHandlerDAOobj.getObject(Company.class.getName(), companyid);
                Company company = (Company) companyResult.getEntityList().get(0);
                StringBuilder storeIds = new StringBuilder();
                if (!includeQAAndRepairStore && !(isRepairStoresOnly && pref.isActivateQAApprovalFlow())) { 
                    storeIds = storeDAO.getQAAndRepairStoreId(company);
                }
                //prepare a list of stores to exclude and display the remaining ones
                List<Store> excludingstoreids = new ArrayList<>();
                if (isRepairStoresOnly) { //only get repair type stores hence exclude the rest
                    excludingstoreids = storeDAO.getStores(pref.getCompany(), true, new StoreType[]{StoreType.RETAIL, StoreType.WAREHOUSE, StoreType.HEADQUARTER, StoreType.SCRAP}, null, null, true, false, false);
                } else if (isScrapStoreOnly) { //only get scrap type stores hence exclude the rest
                    excludingstoreids = storeDAO.getStores(pref.getCompany(), true, new StoreType[]{StoreType.RETAIL, StoreType.WAREHOUSE, StoreType.HEADQUARTER, StoreType.REPAIR}, null, null, true, false, false);
                } else if (isScrapStoreOnly && isRepairStoresOnly) { //get only scrap and repair stores
                    excludingstoreids = storeDAO.getStores(pref.getCompany(), true, new StoreType[]{StoreType.RETAIL, StoreType.WAREHOUSE, StoreType.HEADQUARTER}, null, null, true, false, false);
                } else { //get the rest and exclude Repair/Scrap stores
                    excludingstoreids = storeDAO.getStores(pref.getCompany(), true, new StoreType[]{StoreType.SCRAP, StoreType.REPAIR}, null, null, true, false, false);
                }
                String storeids = "";
                List<String> storelist = new ArrayList<>();
                for (Store store : excludingstoreids) {
                    storelist.add(store.getId());
                }
               //creating delimited string for IN query
                storeids = org.springframework.util.StringUtils.collectionToDelimitedString(storelist, ",", "'", "'"); 
                
               //Exclude QA store as well  
                if (!includeQAAndRepairStore && !StringUtil.isNullOrEmpty(storeids) && !StringUtil.isNullOrEmpty(pref.getInspectionStore())) {
                    storeids += ",'" + pref.getInspectionStore() + "'";
                }
                if (!StringUtil.isNullOrEmpty(storeids)) {
                    if (StringUtil.isNullOrEmpty(storeIds.toString())) { //if existing ids are empty do not append with comma initially
                        storeIds.append(storeids);
                    } else {
                        storeIds.append(",").append(storeids);
                    }
                }
                
                if (!StringUtil.isNullOrEmpty(storeIds.toString())) {
                    filter_names.add("NOTINs.id");
                    filter_params.add(storeIds);
                }
            }
            order_by.add("name");
            order_type.add("asc");
            filterRequestParams.put("filter_names", filter_names);
            filterRequestParams.put("filter_params", filter_params);
            filterRequestParams.put("order_by", order_by);
            filterRequestParams.put("order_type", order_type);
            KwlReturnObject result = accMasterItemsDAOobj.getWarehouseItems(filterRequestParams);

            List list = result.getEntityList();
            Iterator itr = list.iterator();
            //ERP-38095 in QA approval flow default repair store was not being populated due to this flag being false
            if (pref.isActivateQAApprovalFlow() && isRepairStoresOnly) {
                includeQAAndRepairStore = true; //enabling this flag as the default repair store needs to be shown and QA store has been excluded above already
            }
            List<Store> storeL;
            storeL = storeDAO.getStoresByStoreExecutivesAndManagers(loginUser, true, null, searchString, null,includeQAAndRepairStore,includePickandPackStore);
            List<String> storeList = new ArrayList<>();
                for (Store store : storeL) {
                    storeList.add(store.getId());
                }
            JSONArray jArr = new JSONArray();
            while (itr.hasNext()) {
                InventoryWarehouse inventoryWarehouse = (InventoryWarehouse) itr.next();
                JSONObject obj = new JSONObject();
                obj.put("id", inventoryWarehouse.getId());
                if (storeList.contains(obj.get("id"))) {
                KwlReturnObject res = accountingHandlerDAOobj.getObject(Store.class.getName(), inventoryWarehouse.getId());
                Store store = (Store) res.getEntityList().get(0);
                if (store != null) {
                    obj.put("name", store.getFullName());
                }
                //                obj.put("name", inventoryWarehouse.getName());
                obj.put("parentid", inventoryWarehouse.getParentId() != null ? inventoryWarehouse.getParentId() : "");
                obj.put("parent", inventoryWarehouse.getParent() != null ? inventoryWarehouse.getParent().getName() : "");
                obj.put("locationid", inventoryWarehouse.getLocation() != null ? inventoryWarehouse.getLocation().getId() : "");
                obj.put("location", inventoryWarehouse.getLocation() != null ? inventoryWarehouse.getLocation().getName() : "");
                obj.put("isdefault", inventoryWarehouse.isIsdefault());
                jArr.put(obj);
            }
            }
            jobj.put("data", jArr);
        } catch (JSONException e) {
            throw ServiceException.FAILURE(e.getMessage(), e);
        }
        return jobj;
    }
    public ModelAndView saveSalesComissionScehma(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("MI_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        try {
            KwlReturnObject result = saveSalesComissionScehma(request);
            issuccess = true;
            msg = result.getMsg();
            if (result.getEntityList() != null) {
                SalesComissionScehma salesComissionScehma = (SalesComissionScehma) result.getEntityList().get(0);
                jobj.put("id", salesComissionScehma.getID());
            }
             txnManager.commit(status);

        } catch (Exception ex) {
            txnManager.rollback(status);
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
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public KwlReturnObject saveSalesComissionScehma(HttpServletRequest request) throws SessionExpiredException, ServiceException {
        KwlReturnObject result = null;
        String msg = messageSource.getMessage("acc.master.save", null, RequestContextUtils.getLocale(request));   //"Master item has been saved successfully";
        boolean isPresent = true;
        String itemID = request.getParameter("itemid");
        int commissionType = Integer.parseInt((String) request.getParameter("commissiontype"));
        HashMap requestParam = AccountingManager.getGlobalParams(request);
        requestParam.put("itemid", itemID);
        if (!StringUtil.isNullOrEmpty(request.getParameter("id"))) {
            requestParam.put("id", request.getParameter("id"));
        }
        if (commissionType == 1 || commissionType == 3) {//For Invoice Amount OR Payment Term
            requestParam.put("lowerlimit", request.getParameter("lowerlimit"));
            requestParam.put("upperlimit", request.getParameter("upperlimit"));
        } else if(commissionType == 2){//For Brand 
            requestParam.put("categoryid", request.getParameter("categoryid"));
        }else if(commissionType == 4){//For product commission rule
            requestParam.put("productid", request.getParameter("productid"));
        }

        requestParam.put("commissiontype", commissionType);
        requestParam.put("percentage", request.getParameter("percentage"));
        requestParam.put("amount", request.getParameter("amount"));
        result = accMasterItemsDAOobj.saveSalesComissionScehma(requestParam);
        
        action = !StringUtil.isNullOrEmpty(request.getParameter("id")) ? " has updated" : " has added";
        MasterItem masterItem = (MasterItem) kwlCommonTablesDAOObj.getClassObject(MasterItem.class.getName(), itemID);
        auditTrailObj.insertAuditLog(AuditAction.SALESPERSON_SALESCOMISSIONSCEHMA, "User " + sessionHandlerImpl.getUserFullName(request) + action +" Sales Comission Scehma for Sales Person " + "<b>" + masterItem.getValue() + "</b>" + " ( " + masterItem.getCode() + " ) ", request, itemID);

        return new KwlReturnObject(true, msg, null, result.getEntityList(), 0);

    }

    public ModelAndView getSalesComissionScehma(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        try {
            jobj = getSalesComissionScehma(request);
            issuccess = true;
        } catch (SessionExpiredException ex) {
            msg = ex.getMessage();
            Logger.getLogger(accMasterItemsController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ServiceException ex) {
            msg = ex.getMessage();
            Logger.getLogger(accMasterItemsController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            msg = "accMasterItemsController.getsalesComissionScehma : " + ex.getMessage();
            Logger.getLogger(accMasterItemsController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accMasterItemsController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public JSONObject getSalesComissionScehma(HttpServletRequest request) throws ServiceException, SessionExpiredException {
        JSONObject jobj = new JSONObject();
        try {
            HashMap<String, Object> filterRequestParams = new HashMap<String, Object>();
            ArrayList filter_names = new ArrayList(), filter_params = new ArrayList(), order_by = new ArrayList(), order_type = new ArrayList();
            filter_names.add("company.companyID");
            filter_params.add(sessionHandlerImpl.getCompanyid(request));
            filter_names.add("schemaItem");
            filter_params.add(request.getParameter("itemid"));
            filter_names.add("commissiontype");
            filter_params.add(Integer.parseInt((String) request.getParameter("commissiontype")));
            order_by.add("lowerlimit");
            order_by.add("upperlimit");
            order_type.add("asc");
            order_type.add("asc");
            filterRequestParams.put("filter_names", filter_names);
            filterRequestParams.put("filter_params", filter_params);
            filterRequestParams.put("order_by", order_by);
            filterRequestParams.put("order_type", order_type);
            KwlReturnObject result = accMasterItemsDAOobj.getsalesComissionScehma(filterRequestParams);

            List list = result.getEntityList();
            Iterator itr = list.iterator();
            JSONArray jArr = new JSONArray();
            while (itr.hasNext()) {
                SalesComissionScehma salesComissionScehma = (SalesComissionScehma) itr.next();
                JSONObject obj = new JSONObject();
                obj.put("id", salesComissionScehma.getID());
                obj.put("percentagetype", salesComissionScehma.getPercentageType());
                if (salesComissionScehma.getPercentageType() == 1) {
                    obj.put("percentagevalue", "Percentage");
                } else {
                    obj.put("percentagevalue", "Flat");
                }
                obj.put("amount", salesComissionScehma.getAmount());
                obj.put("itemid", salesComissionScehma.getSchemaItem());
                int salesComissionType = salesComissionScehma.getCommissiontype();
                obj.put("commissiontype", salesComissionType);

                if (salesComissionType == 1 || salesComissionType == 3) {
                    obj.put("lowerlimit", salesComissionScehma.getLowerlimit());
                    obj.put("upperlimit", salesComissionScehma.getUpperlimit());
                } else if(salesComissionType == 2) {
                    String category = salesComissionScehma.getCategoryid();
                    obj.put("categoryid", category);
                    KwlReturnObject curresult = accountingHandlerDAOobj.getObject(MasterItem.class.getName(), category);
                    MasterItem masterItem = (MasterItem) curresult.getEntityList().get(0);

                    if (masterItem != null) {
                        obj.put("categoryname", masterItem.getValue());
                    }
                } else if(salesComissionType == 4){
                    String productid = salesComissionScehma.getProductId();
                     obj.put("productid", productid);
                     HashMap<String, Object> requestParams = new HashMap<String, Object>();
                     requestParams.put("companyID", sessionHandlerImpl.getCompanyid(request));
                     requestParams.put("productid", productid);
                     /*
                           Added to get product name and product id 
                     */
                     KwlReturnObject result1 = accMasterItemsDAOobj.getProductsForPricingBandMasterDetails(requestParams);
                     List<Object[]> list1 = result1.getEntityList();
                     for (Object[] productObj : list1) {
                         String name =(String) productObj[1];
                         String pid=(String) productObj[2];
                         obj.put("productname", name);
                         obj.put("pid", pid);
                    }
                }

                jArr.put(obj);
            }
            jobj.put("data", jArr);
            jobj.put("count", jArr.length());
        } catch (JSONException e) {
            throw ServiceException.FAILURE(e.getMessage(), e);
        }
        return jobj;
    }

    public ModelAndView deleteSalesComissionScehma(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false, isCommitEx = false;
        String msg = "";
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("MI_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        try {
            int no = deleteSalesComissionScehma(request);
            issuccess = true;
            msg = messageSource.getMessage("acc.master.del", null, RequestContextUtils.getLocale(request));   //"Master item has been deleted successfully";
            try {
                txnManager.commit(status);
            } catch (Exception ex) {
                isCommitEx = true;
                msg = messageSource.getMessage("acc.master.excp1", null, RequestContextUtils.getLocale(request));   //"The Master Item(s) is or had been used in transaction(s). So, it cannot be deleted.";
            }
        } catch (ServiceException ex) {
            if (!isCommitEx) {
                txnManager.rollback(status);
            }
            msg = "" + ex.getMessage();
            Logger.getLogger(accMasterItemsController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            txnManager.rollback(status);
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
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public int deleteSalesComissionScehma(HttpServletRequest request) throws ServiceException, AccountingException {
        String id = request.getParameter("itempriceid");
        int numRows = 0;
        try {

            accMasterItemsDAOobj.deleteSalesComissionScehma(id);
        } catch (ServiceException ex) {
            throw new AccountingException(messageSource.getMessage("acc.master.excp1", null, RequestContextUtils.getLocale(request)));
        }
        return numRows;
    }

    public ModelAndView getMasterItemsForCustom(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        try {
            HashMap<String, Object> filterRequestParams = new HashMap<String, Object>();
            ArrayList filter_names = new ArrayList(), filter_params = new ArrayList(), order_by = new ArrayList(), order_type = new ArrayList();
            filter_names.add("field.id");
            filter_params.add(request.getParameter("groupid"));
            order_by.add("value");
            order_type.add("asc");
            filterRequestParams.put("filter_names", filter_names);
            filterRequestParams.put("filter_params", filter_params);
            filterRequestParams.put("order_by", order_by);
            filterRequestParams.put("order_type", order_type);
            KwlReturnObject result = accMasterItemsDAOobj.getMasterItemsForCustom(filterRequestParams);

            List list = result.getEntityList();
            Iterator itr = list.iterator();
            JSONArray jArr = new JSONArray();
            while (itr.hasNext()) {
                FieldComboData item = (FieldComboData) itr.next();
                JSONObject obj = new JSONObject();
                obj.put("id", item.getId());
                obj.put("name", item.getValue());
                int level = 0;
                level = getDimensionItemLevel(item, level);
                obj.put("parentid", (item.getParent() == null) ? "" : item.getParent().getId());
                obj.put("leaf", (item.getChildren().size() == 0) ? true : false);
                obj.put("level", level);
                jArr.put(obj);
            }
            jobj.put("count", jArr.length());
            jobj.put("data", jArr);
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
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public String getMasterItemsForCustomValuesCommastr(String fieldId) {

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
                    valuesStr += fieldComboData.getValue() + ";";
                } else {
                    valuesStr += fieldComboData.getValue();
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
    
    public ModelAndView getMasterItemsForCustomFoHire(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        try {

            JSONObject paramJobj = StringUtil.convertRequestToJsonObject(request);
            jobj = accMasterItemsService.getMasterItemsForCustomFoHire(paramJobj);
        } catch (Exception e) {
            try {
                throw ServiceException.FAILURE(e.getMessage(), e);
            } catch (ServiceException ex) {
                Logger.getLogger(accMasterItemsController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public int getDimensionItemLevel(FieldComboData fieldComboData, int level) throws ServiceException {
        if (fieldComboData.getParent() != null) {
            level++;
            level = getDimensionItemLevel(fieldComboData.getParent(), level);
        }
        return level;
    }

    public ModelAndView saveMasterGroup(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("MI_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        try {
            KwlReturnObject result = saveMasterGroup(request);
            issuccess = true;
            msg = "Master group has been saved successfully";
            txnManager.commit(status);
        } catch (SessionExpiredException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            Logger.getLogger(accMasterItemsController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ServiceException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            Logger.getLogger(accMasterItemsController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            txnManager.rollback(status);
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
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public KwlReturnObject saveMasterGroup(HttpServletRequest request) throws SessionExpiredException, ServiceException {
        KwlReturnObject result;

        HashMap requestParam = AccountingManager.getGlobalParams(request);
        requestParam.put("id", request.getParameter("id"));
        requestParam.put("name", request.getParameter("name"));

        String groupID = request.getParameter("id");
        if (StringUtil.isNullOrEmpty(groupID)) {
            result = accMasterItemsDAOobj.addMasterGroup(requestParam);
        } else {
            result = accMasterItemsDAOobj.updateMasterGroup(requestParam);
        }
        return result;
    }

    public ModelAndView getMasterData(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        try {
            jobj = getMasterData(request);  
            String companyid = sessionHandlerImpl.getCompanyid(request);
            KwlReturnObject extcmppref = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), companyid);
            ExtraCompanyPreferences extcmpprefObj = (ExtraCompanyPreferences) extcmppref.getEntityList().get(0);
            if (extcmpprefObj != null && extcmpprefObj.isIsNewGST()) {
                jobj.put("companyid", companyid);
                jobj = accMasterItemsService.getAddressMappingForDimension(jobj); // to get Address Field mapped with Dimension.               
            }
            issuccess = true;
        } catch (SessionExpiredException ex) {
            msg = ex.getMessage();
            Logger.getLogger(accMasterItemsController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ServiceException ex) {
            msg = ex.getMessage();
            Logger.getLogger(accMasterItemsController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            msg = "accMasterItemsController.getMasterData : " + ex.getMessage();
            Logger.getLogger(accMasterItemsController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accMasterItemsController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public JSONObject getMasterData(HttpServletRequest request) throws ServiceException, SessionExpiredException {
        JSONObject jobj = new JSONObject();
        try {
//            KwlReturnObject result = accMasterItemsDAOobj.getMasterData(request.getParameter("masterid"));
            boolean isShowCustomColumn = false;
            if (!StringUtil.isNullOrEmpty(request.getParameter("isShowCustColumn"))) {
                isShowCustomColumn = Boolean.parseBoolean(request.getParameter("isShowCustColumn"));
            }
//            List list = result.getEntityList();
//            Iterator iter = list.iterator();
            JSONArray jArr = new JSONArray();
//            while (iter.hasNext()) {
//                MasterGroup mst = (MasterGroup) iter.next();
//                JSONObject tmpObj = new JSONObject();
//                tmpObj.put("id", mst.getID());
////                tmpObj.put("name", mst.getGroupName());
//                tmpObj.put("name", messageSource.getMessage("acc.masterConfig." + mst.getID(), null, RequestContextUtils.getLocale(request)));
//                jArr.put(tmpObj);
//            }
            if (isShowCustomColumn) {
                HashMap<String, Object> requestParams = new HashMap<String, Object>();
                requestParams.put("filter_names", Arrays.asList("companyid"));
                requestParams.put("filter_values", Arrays.asList(sessionHandlerImpl.getCompanyid(request)));
                requestParams.put("filter_names", Arrays.asList("id"));
                requestParams.put("filter_values", Arrays.asList(request.getParameter("masterid")));
                KwlReturnObject result = accMasterItemsDAOobj.getFieldParams(requestParams);

                List list = result.getEntityList();
                Iterator iter = list.iterator();

                while (iter.hasNext()) {
                    FieldParams fieldParams = (FieldParams) iter.next();
                    String fieldName = fieldParams.getFieldlabel();
                    String fieldNameAppendString = (fieldParams.getIsessential() == 1) ? "*" : "";
                    JSONObject tmpObj = new JSONObject();
                    tmpObj.put("id", fieldParams.getId());
                    tmpObj.put("name", fieldName + fieldNameAppendString);
                    tmpObj.put("fieldtooltip", fieldParams.getFieldtooltip() != null ? fieldParams.getFieldtooltip() : "");
                    tmpObj.put("modulename", accMasterItemsService.getModuleName(fieldParams.getModuleid()));
                    tmpObj.put("fieldtype", fieldParams.getFieldtype());
                    tmpObj.put("iscustomfield", fieldParams.getCustomfield() == 1 ? true : false);
                    tmpObj.put("isforproject", fieldParams.getisforproject() == 1 ? true : false);
                    tmpObj.put("isforeclaim", fieldParams.getIsforeclaim()== 1 ? true : false);
                    tmpObj.put("maxlength", fieldParams.getMaxlength());
                    tmpObj.put("mapWithFieldType", fieldParams.getmapwithtype());
                    tmpObj.put("isfortask", fieldParams.getisfortask() == 1 ? true : false);
                    tmpObj.put("itemparentid", fieldParams.getParent() != null ? fieldParams.getParent().getId() : "");
                    tmpObj.put("itemparentValue", fieldParams.getParent() != null ? fieldParams.getParent().getFieldlabel() : "");
                    tmpObj.put("defaultValue", fieldParams.getDefaultValue() != null ? fieldParams.getDefaultValue() : "");
                    tmpObj.put("isforsalescommission", fieldParams.isIsForSalesCommission());
                    tmpObj.put("relatedmoduleisallowedit", fieldParams.getRelatedModuleIsAllowEdit());
                    if (fieldParams.getFieldtype() == 4 || fieldParams.getFieldtype() == 7 || fieldParams.getFieldtype() == 12) {
                        tmpObj.put("combocommvalues", getMasterItemsForCustomValuesCommastr(fieldParams.getId()));
                    }
                    tmpObj.put("isessential", fieldParams.getIsessential() == 1 ? true : false);
                    tmpObj.put("customcolumn", fieldParams.getCustomcolumn() == 1 ? true : false);
                    tmpObj.put(Constants.ISAUTOPOPULATEDEFAULTVALUE, fieldParams.isIsAutoPopulateDefaultValue());
                    tmpObj.put(Constants.isForKnockOff, fieldParams.isIsForKnockOff());
                    tmpObj.put(Constants.IsForGSTRuleMapping, fieldParams.isFieldOfGivenGSTConfigType(Constants.IsForGSTRuleMapping));
                    jArr.put(tmpObj);
                }
            }
            jobj.put("data", jArr);
        } catch (JSONException e) {
            throw ServiceException.FAILURE(e.getMessage(), e);
        }
        return jobj;
    }

    public ModelAndView getMasterGroups(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        try {
            JSONObject paramJobj = StringUtil.convertRequestToJsonObject(request);
            jobj = accMasterItemsService.getMasterGroups(paramJobj);
        } catch (SessionExpiredException ex) {
            Logger.getLogger(accMasterItemsController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(accMasterItemsController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public ModelAndView ExportCustomFilds(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        try {
            int requesttype = Integer.parseInt(request.getParameter("requesttype"));
            if (requesttype == 0) {
                jobj = getExportCustomFildsJson(request);
            } else if ( requesttype == 1 ) {
                jobj = getExportCustomFildsDataJson(request);
            } else if ( requesttype == 2 ) {
                jobj = getExportDefaultFieldsJson(request);
            } 
            exportDaoObj.processRequest(request, response, jobj);
            jobj.put("success", true);
        } catch (SessionExpiredException ex) {
            Logger.getLogger(accMasterItemsController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(accMasterItemsController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public JSONObject getExportDefaultFieldsJson (HttpServletRequest request) throws JSONException, ServiceException, SessionExpiredException {
        JSONObject jobj = new JSONObject();
        try {
            String companyid = sessionHandlerImpl.getCompanyid(request);
            KwlReturnObject result = accMasterItemsDAOobj.getMasterGroups();
            List list = result.getEntityList();
            Iterator iter = list.iterator();
            JSONArray jArr = new JSONArray();
            while ( iter.hasNext() ) {
                MasterGroup masterGroupObj = (MasterGroup) iter.next();
                JSONObject tempJobj = new JSONObject();
                tempJobj.put("fieldlable", messageSource.getMessage("acc.masterConfig." + masterGroupObj.getID(), null, RequestContextUtils.getLocale(request)));
                tempJobj.put("isessential", "Yes");
                String combodata = "";

                ArrayList filter_names = new ArrayList(), filter_params = new ArrayList(), order_by = new ArrayList(), order_type = new ArrayList();
                HashMap<String, Object> filterRequestParams = new HashMap<String, Object>();
                order_by.add("value");
                order_type.add("asc");
                
                if(!masterGroupObj.getID().equals("16")){                       
                    filter_names.add("masterGroup.ID");
                    filter_params.add(masterGroupObj.getID());
                    filter_names.add("company.companyID");
                    filter_params.add(companyid);

                    filterRequestParams.put("filter_names", filter_names);      
                    filterRequestParams.put("filter_params", filter_params);    
                    filterRequestParams.put("order_by", order_by);              
                    filterRequestParams.put("order_type", order_type);          
                    KwlReturnObject result2 = accMasterItemsDAOobj.getMasterItems(filterRequestParams);

                    List<MasterItem> list2 = result2.getEntityList();
                    for (MasterItem item : list2) {
                        combodata += item.getValue()+";";
                    }
                } else {                                                        // For id=16, get data from pricetype
                    filter_names.add("company.companyID");
                    filter_params.add(companyid);
                    
                    filterRequestParams.put("filter_names", filter_names);      
                    filterRequestParams.put("filter_params", filter_params);    
                    filterRequestParams.put("order_by", order_by);              
                    filterRequestParams.put("order_type", order_type);          
                    KwlReturnObject result2 = accMasterItemsDAOobj.getMasterPriceDependentItem(filterRequestParams);

                    List<PriceType> list2 = result2.getEntityList();
                    for (PriceType priceType : list2) {
                        combodata += priceType.getValue()+";";
                    }
                }
                tempJobj.put("combodata", combodata);
                tempJobj.put("modulename", "All");
                jArr.put(tempJobj);
            }
            jobj.put("data", jArr);
        } catch ( Exception ex ) {
            Logger.getLogger(accMasterItemsController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return jobj;
    }
           
    public JSONObject getExportCustomFildsJson(HttpServletRequest request) throws JSONException, ServiceException, SessionExpiredException {
        JSONObject jobj = new JSONObject();
        try {
            KwlReturnObject result = accMasterItemsDAOobj.getMasterGroups();
            List list = result.getEntityList();
            Iterator iter = list.iterator();
            JSONArray jArr = new JSONArray();
            DateFormat df = authHandler.getOnlyDateFormat(request);
    
            boolean isShowCustomColumn = false;
            boolean isShowDimensiononly = false;
            boolean isShowCustomFieldonly = false;
            if (!StringUtil.isNullOrEmpty(request.getParameter("isShowCustColumn"))) {
                isShowCustomColumn = Boolean.parseBoolean(request.getParameter("isShowCustColumn"));
            }
            if (!StringUtil.isNullOrEmpty(request.getParameter("isShowDimensiononly"))) {
                isShowDimensiononly = Boolean.parseBoolean(request.getParameter("isShowDimensiononly"));
            }
            if (!StringUtil.isNullOrEmpty(request.getParameter("isShowCustomFieldonly"))) {
                isShowCustomFieldonly = Boolean.parseBoolean(request.getParameter("isShowCustomFieldonly"));
            }
            if (isShowDimensiononly || isShowCustomFieldonly) {
                isShowCustomColumn = true;
            }
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            if (isShowCustomColumn) {
                if (isShowDimensiononly) {
                    requestParams = new HashMap<String, Object>();
                    requestParams.put("filter_names", Arrays.asList("companyid", "customfield"));
                    requestParams.put("filter_values", Arrays.asList(sessionHandlerImpl.getCompanyid(request), 0));
                }
                if (isShowCustomFieldonly) {
                    requestParams = new HashMap<String, Object>();
                    requestParams.put("filter_names", Arrays.asList("companyid", "customfield"));
                    requestParams.put("filter_values", Arrays.asList(sessionHandlerImpl.getCompanyid(request), 1));
                }
                if(!isShowCustomFieldonly && !isShowDimensiononly){
                    requestParams.put("filter_names", Arrays.asList("companyid"));
                    requestParams.put("filter_values", Arrays.asList(sessionHandlerImpl.getCompanyid(request)));
                }
            } else {
                requestParams.put("filter_names", Arrays.asList("companyid"));
                requestParams.put("filter_values", Arrays.asList(sessionHandlerImpl.getCompanyid(request)));
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
                String relatedModuleIds = "";
                int i = 0;
                while (iterator.hasNext()) {
                    String coma = "";
                    if (i != 0) {
                        coma = " ;";
                    }
                    Object[] temp1 = (Object[]) iterator.next();
                    allNameFromId = allNameFromId + coma + accMasterItemsService.getModuleName(Integer.parseInt(temp1[0].toString()));
                    i++;
                    if (String.valueOf(temp1[1]) != "null") {
                        relatedModuleIds = String.valueOf(temp1[1]);
                    }
                }
    
                JSONObject obj = new JSONObject();
                obj.put("fieldlable", fieldParams.getFieldlabel());
                obj.put("maxlength", (fieldParams.getFieldtype() == 1||fieldParams.getFieldtype() == 13) ? fieldParams.getMaxlength() : "");
                obj.put("isessential", fieldParams.getIsessential() == 0 ? "No" : "Yes");
                obj.put("fieldtype", getXTypeLable(fieldParams.getFieldtype()));
                obj.put("combodata", getMasterItemsForCustomValuesCommastr(fieldParams.getId()));
                obj.put("modulename", allNameFromId);
                obj.put("iseditable", "true".equals(fieldParams.getIseditable()) ? "Yes" : "No");
                obj.put("sendnotification", fieldParams.getsendNotification() == 0 ? "No" : "Yes");
                obj.put("notificationdays", fieldParams.getnotificationDays());
                obj.put("isforproject", fieldParams.getisforproject() == 0 ? "No" : "Yes");
                obj.put("isfortask", fieldParams.getisfortask() == 0 ? "No" : "Yes");
                obj.put("iscustomfield", fieldParams.getCustomfield() == 0 ? "No" : "Yes");
                obj.put("iscustomcolumn", fieldParams.getCustomcolumn() == 0 ? "No" : "Yes");
                String relatedModules = "";
                if (!StringUtil.isNullOrEmpty(relatedModuleIds)) {
                    String[] relatedModule = relatedModuleIds.split(",");
                    for (int j = 0; j < relatedModule.length; j++) {
                        int moduleid = Integer.parseInt(relatedModule[j]);
                        relatedModules += accMasterItemsService.getModuleName(moduleid) + " ;";
                    }
                    if (relatedModules.length() > 0) {
                        relatedModules = relatedModules.substring(0, relatedModules.length() - 1);
                    }
                }
                obj.put("relatedmoduleids", relatedModules);
                obj.put("parentname", fieldParams.getParent() != null ? fieldParams.getParent().getFieldlabel() : "No");
                if (getXTypeLable(fieldParams.getFieldtype()).equalsIgnoreCase("Date Field")) {
                    DateFormat defaultDateFormat=new SimpleDateFormat(Constants.MMMMdyyyy);
                    Date dateFromDB=null;
                    if (!StringUtil.isNullOrEmpty(fieldParams.getDefaultValue())) {
                        if (df != null) {
                            try {
                                dateFromDB = defaultDateFormat.parse(fieldParams.getDefaultValue());
                            } catch (Exception e) {
                            }
                        }
                        obj.put("defaultval", df.format(dateFromDB));
                    } else {
                        obj.put("defaultval", "");
                    }
                } else {
                   obj.put("defaultval", fieldParams.getDefaultValue());
                
                }
       
                jArr.put(obj);

            }

            jobj.put("data", jArr);
        } catch (JSONException e) {
            throw ServiceException.FAILURE(e.getMessage(), e);
        }
        return jobj;

    }

    public JSONObject getExportCustomFildsDataJson(HttpServletRequest request) throws JSONException, ServiceException, SessionExpiredException {
        JSONObject jobj = new JSONObject();

        try {

            JSONArray jArr = new JSONArray();
            HashMap<String, Object> filterRequestParams = new HashMap<String, Object>();
            String companyid = sessionHandlerImpl.getCompanyid(request);
            ArrayList filter_names = new ArrayList(), filter_params = new ArrayList(), order_by = new ArrayList(), order_type = new ArrayList();
            boolean isexportCustom = false;
            boolean isExportDefault = false;
            String exportDataFlag = !StringUtil.isNullOrEmpty(request.getParameter("exportDataFlag")) ? request.getParameter("exportDataFlag") : "2";
            boolean isShowCustomColumn = false;
            boolean isShowDimensiononly = false;
            boolean isShowCustomFieldonly = false;
            if (!StringUtil.isNullOrEmpty(request.getParameter("isShowCustColumn"))) {
                isShowCustomColumn = Boolean.parseBoolean(request.getParameter("isShowCustColumn"));
            }
            if (!StringUtil.isNullOrEmpty(request.getParameter("isShowDimensiononly"))) {
                isShowDimensiononly = Boolean.parseBoolean(request.getParameter("isShowDimensiononly"));
            }
            if (!StringUtil.isNullOrEmpty(request.getParameter("isShowCustomFieldonly"))) {
                isShowCustomFieldonly = Boolean.parseBoolean(request.getParameter("isShowCustomFieldonly"));
            }
            if (isShowDimensiononly || isShowCustomFieldonly) {
                isShowCustomColumn = true;
            }
            if (exportDataFlag.equals("0")) {
                isexportCustom = true;
            } else if (exportDataFlag.equals("1")) {
                isExportDefault = true;
            } else if (exportDataFlag.equals("2")) {
                isExportDefault = true;
                isexportCustom = true;
            }
            if (isExportDefault) {
                if (isShowCustomColumn) {
                    if (isShowDimensiononly) {
                        filter_names.add("customfield");
                        filter_params.add(0);
                    }
                    if (isShowCustomFieldonly) {
                        filter_names.add("customfield");
                        filter_params.add(1);
                    }
                }
                filter_names.add("company.companyID");
                filter_params.add(sessionHandlerImpl.getCompanyid(request));
                order_by.add("value");
                order_type.add("asc");
                filterRequestParams.put("filter_names", filter_names);
                filterRequestParams.put("filter_params", filter_params);
                filterRequestParams.put("order_by", order_by);
                filterRequestParams.put("order_type", order_type);
                KwlReturnObject mastergroupresult = accMasterItemsDAOobj.getMasterItems(filterRequestParams);
                List masterlist = mastergroupresult.getEntityList();
                Iterator masteritr = masterlist.iterator();
                while (masteritr.hasNext()) {
                    MasterItem item = (MasterItem) masteritr.next();
                    JSONObject obj = new JSONObject();
                    obj.put("data", item.getValue());
                    obj.put("fieldname", messageSource.getMessage("acc.masterConfig." + item.getMasterGroup().getID(), null, RequestContextUtils.getLocale(request))); 
                    obj.put("parent", item.getParent() != null ? item.getParent().getValue() : "No");
                    obj.put("extparentdimen", "No");
                    obj.put("extparent", "No");
                    obj.put("ismastergroup", "Yes");
                    jArr.put(obj);
                }
            }
            if (isexportCustom) {
                filter_names.clear();
                filter_params.clear();
                filterRequestParams.clear();
                if (isShowCustomColumn) {
                    if (isShowDimensiononly) {
                        filter_names.add("customfield");
                        filter_params.add(0);
                    }
                    if (isShowCustomFieldonly) {
                        filter_names.add("customfield");
                        filter_params.add(1);
                    }
                }
                filter_names.add("field.company.companyID");
                filter_params.add(companyid);
                filterRequestParams.put("filter_names", filter_names);
                filterRequestParams.put("filter_params", filter_params);
                KwlReturnObject result = accMasterItemsDAOobj.getMasterItemsForCustom(filterRequestParams);
                List list = result.getEntityList();
                List<String> list_of_distinctrec = new ArrayList<String>();
                Iterator iter = list.iterator();
                while (iter.hasNext()) {
                    JSONObject obj = new JSONObject();
                    FieldComboData fcd = (FieldComboData) iter.next();
                    if (list_of_distinctrec.contains(fcd.getValue() + fcd.getField().getFieldlabel())) {
                        continue;
                    }
                    list_of_distinctrec.add(fcd.getValue() + fcd.getField().getFieldlabel());
                    obj.put("data", fcd.getValue());
                    obj.put("fieldname", fcd.getField().getFieldlabel());
                    obj.put("parent", fcd.getParent() != null ? fcd.getParent().getValue() : "No");
                    try {
                        obj.put("extparentdimen", fcd.getField().getParent().getFieldlabel());
                    } catch (Exception e) {
                        obj.put("extparentdimen", "No");
                    }
                    ArrayList filter_nameschield = new ArrayList(), filter_paramsChield = new ArrayList();
                    HashMap<String, Object> filterRequestParamsChild = new HashMap<String, Object>();
                    filter_nameschield.add("child.id");
                    filter_paramsChield.add(fcd.getId());
                    filterRequestParamsChild.put("filter_names", filter_nameschield);
                    filterRequestParamsChild.put("filter_params", filter_paramsChield);
                    KwlReturnObject resultChildData = accMasterItemsDAOobj.getMasterItemsParentDimensionValue(filterRequestParamsChild);
                    List<FieldComboDataMapping> lst = resultChildData.getEntityList();
                    String externalParents = "";
                    for (FieldComboDataMapping fcdMap : lst) {
                        externalParents += fcdMap.getParent().getValue() + " ;";
                    }
                    if (externalParents.length() > 1) {
                        externalParents = externalParents.substring(0, externalParents.length() - 1);
                    }
                    obj.put("extparent", StringUtil.isNullOrEmpty(externalParents) ? "No" : externalParents);
                    obj.put("ismastergroup", "No");
                    jArr.put(obj);
                }
            }
            jobj.put("data", jArr);
        } catch (JSONException e) {
            throw ServiceException.FAILURE(e.getMessage(), e);
        }
        return jobj;

    }

    private String getXTypeLable(int fieldType) {
        switch (fieldType) {
            case 1:
                return "Text Field";
            case 2:
                return "Number Field";
            case 3:
                return "Date Field";
            case 4:
                return "Drop Down";
            case 5:
                return "Time Field";
            case 6:
                return "Check Box";
            case 7:
                return "Select";
            case 8:
                return "Drop Down";
            case 9:
                return "Autono";
            case 11:
                return "Check Box";
            case 13:
                return "Text Area";
        }
        return "";
    }

    public ModelAndView deleteMasterGroup(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("MI_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        try {
            KwlReturnObject result = accMasterItemsDAOobj.deleteMasterGroup("groupid");
            issuccess = true;
            msg = "Master group has been deleted successfully";
            txnManager.commit(status);
        } catch (ServiceException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            Logger.getLogger(accMasterItemsController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            txnManager.rollback(status);
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
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public ModelAndView deleteMasterCustomItem(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false, isCommitEx = false;
        boolean isUsed = false;
        String auditaction="";
        String msg = "";
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("MI_Tx");
        String moduleIds = request.getParameter("moduleIds");
        String comIds[] = moduleIds.split(",");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        HashMap <String, String> parentFieldComboDataIds=new HashMap<>();
        try {
            String idsString=StringUtil.isNullOrEmpty(request.getParameter("ids"))?"":request.getParameter("ids");
            String ids[] = idsString.split(",");
            String userid = sessionHandlerImpl.getUserid(request);
            // String companyid = sessionHandlerImpl.getCompanyid(request);
            for (int j = 0; j < ids.length; j++) {

                HashMap<String, String> paresntIds = accMasterItemsDAOobj.getParentIds(comIds, ids[j]);
                parentFieldComboDataIds=(HashMap<String, String>) paresntIds.clone();
                for (int k = 0; k < comIds.length; k++) {
                    //String grpId = request.getParameter("groupid");
                    String grpId = comIds[k];
                    int numRows = 0;
                    // for (int i = 0; i < comIds.length; i++) {
                    //isUsed = accMasterItemsDAOobj.isUsedMasterCustomItem(ids[i], grpId);
                    isUsed = accMasterItemsDAOobj.isUsedMasterCustomItem(paresntIds.get(grpId), grpId);
                    if (isUsed) {
                        break;
                    }
                    //}
                    if (!isUsed) {
                        //for (int i = 0; i < ids.length; i++) {
                        if (paresntIds.get(grpId) != null) {
                            //Check use in Eclaim
                            FieldParams dimention = (FieldParams) kwlCommonTablesDAOObj.getClassObject(FieldParams.class.getName(), grpId);
                            if (dimention != null && dimention.getIsforeclaim() == 1) {
                                HashMap<String, Object> params = new HashMap<String, Object>();
                                FieldComboData combo = (FieldComboData) kwlCommonTablesDAOObj.getClassObject(FieldComboData.class.getName(), paresntIds.get(grpId));
                                JSONArray jArr = new JSONArray();
                                JSONObject obj = new JSONObject();
                                obj.put("eclaimid", combo.getEclaimid());
                                obj.put("appuiid", combo.getId());
                                jArr.put(obj);
                                params.put("userid", userid);
                                params.put("ids", jArr);
                                params.put("companyid", dimention.getCompanyid());
                                params.put("servletContext", this.getServletContext());
                                accMasterItemsService.checkUseInEclaim(params);
                            }
                            Map<String, Object> map = new HashMap();
                            map.put("masterItem", grpId);
                            /**
                             * Delete FCD and Users GRP Mapping
                             */
                                accMasterItemsDAOobj.deleteUsersGroupFieldComboMappingUsingFCD(map);
                                accMasterItemsDAOobj.daleteMasterCustomItem(paresntIds.get(grpId));
                            }
                        numRows++;
                        //}
                        issuccess = true;
                        msg = messageSource.getMessage("acc.master.del", null, RequestContextUtils.getLocale(request));   //"Master item has been deleted successfully";
                    } else {
                        isCommitEx = true;
                        issuccess = true;
                        msg = messageSource.getMessage("acc.master.excp1", null, RequestContextUtils.getLocale(request));//The Master Item(s) is or had been used in transaction(s). So, it cannot be deleted.
                        }
                    }
                }
            try {
                if (isUsed) {
                    msg = messageSource.getMessage("acc.master.excp1", null, RequestContextUtils.getLocale(request));//The Master Item(s) is or had been used in transaction(s). So, it cannot be deleted.
                    txnManager.rollback(status);
                } else {
                    String customcolumn = request.getParameter("customcolumn");
                    String groupname[] = request.getParameterValues("name");
                    String masterGroup = request.getParameter("groupname");
                    boolean iscustom = StringUtil.isNullOrEmpty(request.getParameter("iscustom")) ? false : Boolean.parseBoolean(request.getParameter("iscustom"));
                    String action = "dimension";
                     auditaction=(action.equalsIgnoreCase("added")?AuditAction.DIMENTION_ADDED:AuditAction.DIMENTION_UPDATED);
                    if (iscustom == true) {
                        if (customcolumn.equals("1")) {
                            action = "custom column";
                            auditaction=(action.equalsIgnoreCase("added")?AuditAction.CUSTOM_COLUMN_ADDED:AuditAction.CUSTOM_COLUMN_UPDATED);
                        } else {
                            action = "custom field";
                            auditaction=(action.equalsIgnoreCase("added")?AuditAction.CUSTOM_FIELD_ADDED:AuditAction.CUSTOM_FIELD_UPDATED);
                        }

                    }
                    for (int i = 0; i < groupname.length; i++) {
                        auditTrailObj.insertAuditLog(auditaction, "User " + sessionHandlerImpl.getUserFullName(request) + " has deleted master item " + groupname[i] + " for " + action + " " + masterGroup, request, "0");
                    }
                    txnManager.commit(status);
                }
            } catch (Exception ex) {
                isCommitEx = true;
                msg = messageSource.getMessage("acc.master.excp1", null, RequestContextUtils.getLocale(request));   //"The Master Item(s) is or had been used in transaction(s). So, it cannot be deleted.";
            }
            //*************************************
            boolean propagateTOChildCompaniesFalg = false;

            if (!StringUtil.isNullOrEmpty(request.getParameter("ispropagatetochildcompanyflag"))) {
                propagateTOChildCompaniesFalg = Boolean.parseBoolean(request.getParameter("ispropagatetochildcompanyflag"));
            }
            if (propagateTOChildCompaniesFalg) {

                for (Map.Entry<String, String> entry : parentFieldComboDataIds.entrySet()) {
                    String parentid = entry.getValue();

                    HashMap<String, Object> filterRequestParams = new HashMap<String, Object>();
                    ArrayList filter_names = new ArrayList(), filter_params = new ArrayList();
                    filter_names.add("propagatedfieldcomboID.id");
                    filter_params.add(parentid);

                    filterRequestParams.put("filter_names", filter_names);
                    filterRequestParams.put("filter_params", filter_params);
                    KwlReturnObject cntResult = accMasterItemsDAOobj.getMasterItemsForCustom(filterRequestParams);
                    List childCompaniesFieldComboDataList = cntResult.getEntityList();
                    Company company = null;
                    for (Object childObj : childCompaniesFieldComboDataList) {
                        FieldComboData childfieldComboData = (FieldComboData) childObj;
                        try {
                            if (childfieldComboData != null) {
                                status = txnManager.getTransaction(def);
                                String childcompanyfieldcombodataid = childfieldComboData.getId();
                                String childCompanyID = childfieldComboData.getField().getCompanyid();
                                company = (Company) kwlCommonTablesDAOObj.getClassObject(Company.class.getName(), childCompanyID);
                                deletePropagatedMasterCustomItem(request, childCompanyID, childfieldComboData.getField().getId(), childcompanyfieldcombodataid);
//                                    
                                txnManager.commit(status);
                                status = null;
                                auditTrailObj.insertAuditLog(auditaction, "User " + sessionHandlerImpl.getUserFullName(request) + " has deleted masteritem" + childfieldComboData.getValue() + " to child company " + company.getCompanyName(), request, childfieldComboData.getId());
                            }
                        } catch (Exception ex) {
                            txnManager.rollback(status);
                            auditTrailObj.insertAuditLog(auditaction, "User " + sessionHandlerImpl.getUserFullName(request) + " failed to  delete masteritem" + childfieldComboData.getValue() + " to child company " + company.getCompanyName(), request, childfieldComboData.getId());
                        }
                    }
                }
            }
            //*******************
        } catch (ServiceException ex) {
            if (!isCommitEx) {
                txnManager.rollback(status);
            }
            msg = "" + ex.getMessage();
            Logger.getLogger(accMasterItemsController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            txnManager.rollback(status);
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
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    
    public ModelAndView validateEntityCustomFieldUsage(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();                
        boolean issuccess = false;
        String msg = "";
        JSONObject dimensionConfig = new JSONObject();
        try {
            JSONObject params = StringUtil.convertRequestToJsonObject(request);
            String companyid = sessionHandlerImpl.getCompanyid(request);
            params.put("companyid", companyid);
            dimensionConfig=accMasterItemsService.validateEntityCustomFieldUsage(params);  
            jobj.put("dimensionConfig", dimensionConfig);
            issuccess = true;
        } catch (Exception ex) {
            msg = "" + ex.getMessage();
            if (ex.getMessage() == null) {
                msg = ex.getCause().getMessage();
            }
            Logger.getLogger(accMasterItemsController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accMasterItemsController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }        
     public void deletePropagatedMasterCustomItem(HttpServletRequest request,String childCompanyID,String fieldid,String childcompanyfieldcombodataid) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false, isCommitEx = false;
        boolean isUsed = false;
        String msg = "";
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("MI_Tx");
        String comIds[] =new String[]{fieldid};
        String ids[] = new String[]{childcompanyfieldcombodataid};
        
        
         String customcolumn = request.getParameter("customcolumn");
         String groupname[] = request.getParameterValues("name");
         String masterGroup = request.getParameter("groupname");
         boolean iscustom = StringUtil.isNullOrEmpty(request.getParameter("iscustom")) ? false : Boolean.parseBoolean(request.getParameter("iscustom"));

        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        try {

            for (int j = 0; j < ids.length; j++) {

                HashMap<String, String> paresntIds = accMasterItemsDAOobj.getParentIds(comIds, ids[j]);
                for (int k = 0; k < comIds.length; k++) {
                    String grpId = comIds[k];
                    int numRows = 0;
                    isUsed = accMasterItemsDAOobj.isUsedMasterCustomItem(paresntIds.get(grpId), grpId);
                    if (isUsed) {
                        break;
                    }
                    if (!isUsed) {
                        if (paresntIds.get(grpId) != null) {
                         
                            
                            accMasterItemsDAOobj.daleteMasterCustomItem(paresntIds.get(grpId));
                        }
                        numRows++;
                        //}
                        issuccess = true;
                        msg = messageSource.getMessage("acc.master.del", null, RequestContextUtils.getLocale(request));   //"Master item has been deleted successfully";
                    } else {
                        isCommitEx = true;
                        issuccess = true;
                        msg = messageSource.getMessage("acc.master.excp1", null, RequestContextUtils.getLocale(request));//The Master Item(s) is or had been used in transaction(s). So, it cannot be deleted.
                    }
                }
            }
            try {
                if (isUsed) {
                    msg = messageSource.getMessage("acc.master.excp1", null, RequestContextUtils.getLocale(request));//The Master Item(s) is or had been used in transaction(s). So, it cannot be deleted.
                    txnManager.rollback(status);
                } else {
                 
                    String action = "dimension";
                    String auditaction=(action.equalsIgnoreCase("added")?AuditAction.DIMENTION_ADDED:AuditAction.DIMENTION_UPDATED);
                    if (iscustom == true) {
                        if (customcolumn.equals("1")) {
                            action = "custom column";
                            auditaction=(action.equalsIgnoreCase("added")?AuditAction.CUSTOM_COLUMN_ADDED:AuditAction.CUSTOM_COLUMN_UPDATED);
                        } else {
                            action = "custom field";
                            auditaction=(action.equalsIgnoreCase("added")?AuditAction.CUSTOM_FIELD_ADDED:AuditAction.CUSTOM_FIELD_UPDATED);
                        }

                    }
                    for (int i = 0; i < groupname.length; i++) {
                        auditTrailObj.insertAuditLog(auditaction, "User " + sessionHandlerImpl.getUserFullName(request) + " has deleted master item " + groupname[i] + " for " + action + " " + masterGroup, request, "0");
                    }
                    txnManager.commit(status);
                }
            } catch (Exception ex) {
                isCommitEx = true;
                msg = messageSource.getMessage("acc.master.excp1", null, RequestContextUtils.getLocale(request));   //"The Master Item(s) is or had been used in transaction(s). So, it cannot be deleted.";
            }
        } catch (ServiceException ex) {
            if (!isCommitEx) {
                txnManager.rollback(status);
            }
            msg = "" + ex.getMessage();
            Logger.getLogger(accMasterItemsController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            txnManager.rollback(status);
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

    public ModelAndView deleteDimension(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        try {
            JSONObject paramJobj = StringUtil.convertRequestToJsonObject(request);
            paramJobj.put("servletContext", this.getServletContext());
            jobj = accMasterItemsService.deleteDimension(paramJobj);
            issuccess = jobj.optBoolean("success");
            msg = jobj.optString("msg");
        } catch (ServiceException ex) {
            msg = "" + ex.getMessage();
            Logger.getLogger(accMasterItemsController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
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
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    
       public ModelAndView getPackages(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        String cash = "";
        boolean issuccess = false;
        try {
            String start = request.getParameter(Constants.start);        
            String limit = request.getParameter(Constants.limit);
            HashMap<String, Object> requestParams = new HashMap<String, Object>();           
            requestParams.put("companyid", sessionHandlerImpl.getCompanyid(request));
            KwlReturnObject result = accMasterItemsDAOobj.getPackages(requestParams);
            List list = result.getEntityList();
            JSONArray jArr = getPackagesJson(list);
            JSONArray pagedJson = jArr;//ERP-13640 [SJ]                
                if (!StringUtil.isNullOrEmpty(start) && !StringUtil.isNullOrEmpty(limit)) {
                    pagedJson = StringUtil.getPagedJSON(jArr, Integer.parseInt(start), Integer.parseInt(limit));
                }
            jobj.put("data", pagedJson);
            jobj.put("count", jArr.length());
            issuccess = true;
        } catch (SessionExpiredException ex) {
            msg = ex.getMessage();
            Logger.getLogger(accMasterItemsController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
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
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public JSONArray getPackagesJson(List list) throws ServiceException {
        JSONArray jArr = new JSONArray();
        try {
            Iterator itr = list.iterator();
            while (itr.hasNext()) {
                Packages pkg = (Packages) itr.next();
                JSONObject obj = new JSONObject();
                obj.put("packageid", pkg.getPackageid());
                obj.put("packagename", pkg.getPackagename());
                obj.put("measurement", pkg.getMeasurement());
                obj.put("packageweight",pkg.getPackageweight());
                jArr.put(obj);
            }
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("getTermJson : " + ex.getMessage(), ex);
        }
        return jArr;
    }

    public ModelAndView savePackages(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "",usedPackages="";
        boolean issuccess = false;

        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("Package_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);

        TransactionStatus status = txnManager.getTransaction(def);
        try {
            usedPackages=savePackages(request);
            if (!StringUtil.isNullOrEmpty(usedPackages)) {
                msg = messageSource.getMessage("acc.field.packagemasterexcept", null,RequestContextUtils.getLocale(request))+" "+"<b>"+usedPackages.substring(0,usedPackages.length()-1)+"</b>"+" "+messageSource.getMessage("acc.package.updateremain", null,RequestContextUtils.getLocale(request));
            } else {
                msg = messageSource.getMessage("acc.package.update", null, RequestContextUtils.getLocale(request));   //"Package has been Updated successfully";
            }
            issuccess = true;
            txnManager.commit(status);
        } catch (SessionExpiredException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            Logger.getLogger(accMasterItemsController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            txnManager.rollback(status);
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
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public String savePackages(HttpServletRequest request) throws AccountingException, ServiceException, SessionExpiredException {
        String packageid = "",usedPackages="";
        try {
            int delCount = 0;
            JSONArray jArr = new JSONArray(request.getParameter("data"));
            JSONArray jDelArr = new JSONArray(request.getParameter("deleteddata"));
            String companyid = sessionHandlerImpl.getCompanyid(request);
            
            KwlReturnObject packagesresulet;
            HashMap<String, Object> requestParams = new HashMap<String, Object>();           
            requestParams.put("companyid", companyid);
            for (int i = 0; i < jDelArr.length(); i++) {
                JSONObject jobj = jDelArr.getJSONObject(i);
                if (StringUtil.isNullOrEmpty(jobj.getString("packageid")) == false) {
                    packageid = jobj.getString("packageid");
                    requestParams.put("packageid", packageid);
                    String methodname = jobj.getString("packagename");
                    
                    /* Below method is used to check if Pakage is used in Transaction or not*/
                    KwlReturnObject result = accMasterItemsDAOobj.isPackageUsedInTransaction(requestParams);
                    List list = result.getEntityList();
                    
                    if (list.size() > 0) {
                        usedPackages = usedPackages + "" + methodname + ",";
                        continue;
                    }
                    try {
                        packagesresulet = accMasterItemsDAOobj.deletePackage(packageid, companyid);
                        delCount += packagesresulet.getRecordTotalCount();
                        auditTrailObj.insertAuditLog(AuditAction.PAYMENT_METHOD_DELETED, "User " + sessionHandlerImpl.getUserFullName(request) + " has deleted Package " + methodname, request, "0");
                    } catch (ServiceException ex) {
                        throw new AccountingException(messageSource.getMessage("acc.uom.excp1", null, RequestContextUtils.getLocale(request)));
                    }
                }
            }

            Packages packages;
            String auditMsg;
            String auditID;
            for (int i = 0; i < jArr.length(); i++) {
                JSONObject jobj = jArr.getJSONObject(i);
                if (jobj.getBoolean("modified") == false) {
                    continue;
                }

                HashMap<String, Object> packageMap = new HashMap<String, Object>();
                packageMap.put("packagename", StringUtil.DecodeText(jobj.optString("packagename")));
                packageMap.put("measurement", StringUtil.DecodeText(jobj.optString("measurement")));
                packageMap.put("packageweight", Double.parseDouble(StringUtil.DecodeText(jobj.optString("packageweight"))));
                packageMap.put("companyid", companyid);

                if (StringUtil.isNullOrEmpty(StringUtil.DecodeText(jobj.optString("packageid")))) {
                    auditMsg = "added";
                    auditID = AuditAction.PAYMENT_METHOD_ADDED;
                    packagesresulet = accMasterItemsDAOobj.addPackages(packageMap);
                } else {
                    auditMsg = "updated";
                    auditID = AuditAction.PAYMENT_METHOD_CHANGED;
                    packageMap.put("packageid", jobj.getString("packageid"));
                    packagesresulet = accMasterItemsDAOobj.updatePackages(packageMap);
                }
                packages = (Packages) packagesresulet.getEntityList().get(0);

                auditTrailObj.insertAuditLog(auditID, "User " + sessionHandlerImpl.getUserFullName(request) + " has " + auditMsg + " Package " + packages.getPackagename(), request, packages.getPackageid());
            }
        }/* catch (UnsupportedEncodingException ex) {
            throw ServiceException.FAILURE(messageSource.getMessage("acc.uom.excp2", null, RequestContextUtils.getLocale(request)), ex);
        } */catch (JSONException ex) {
            throw ServiceException.FAILURE("savePackages: " + ex.getMessage(), ex);
        }
        return usedPackages;
    }
    
    public ModelAndView savePricingBand(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false, isCommitEx = false;
        String msg = "";
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("Account_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        try {
            KwlReturnObject result = savePricingBandItem(request);
            issuccess = true;
            msg = result.getMsg();
            if (result.getEntityList() != null) {
                PricingBandMaster pricingBandMaster = (PricingBandMaster) result.getEntityList().get(0);
                jobj.put("id", pricingBandMaster.getID());
                String name = request.getParameter("name");
                boolean isEdit = StringUtil.isNullOrEmpty(request.getParameter("isEdit")) ? false : Boolean.parseBoolean(request.getParameter("isEdit"));
                boolean isCopy = StringUtil.isNullOrEmpty(request.getParameter("isCopy")) ? false : Boolean.parseBoolean(request.getParameter("isCopy"));
                String action = "added";
                if (isEdit && !isCopy) {
                    action = "updated";
                }
                auditTrailObj.insertAuditLog(AuditAction.PRICING_BAND_CHANGED, "User " + sessionHandlerImpl.getUserFullName(request) + " has " + action + " Pricing Band item " + name, request, "0");

            } else {
                issuccess = false;
            }
            
            try {
            txnManager.commit(status);
            } catch (Exception ex) {
                isCommitEx = true;
            }

        } catch (Exception ex) {
            if (!isCommitEx) {
                txnManager.rollback(status);
            }
            issuccess = false;
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
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public KwlReturnObject savePricingBandItem(HttpServletRequest request) throws SessionExpiredException, ServiceException {
        KwlReturnObject result = null;
        String msg = messageSource.getMessage("acc.master.save", null, RequestContextUtils.getLocale(request));   // "Master item has been saved successfully";
        boolean isPresent = false;
        String itemID = request.getParameter("id");
        boolean isDefaultToPOS = false;
        boolean isIncludingGst = false;
        if (!StringUtil.isNullOrEmpty(request.getParameter("isDefaultToPOS"))) {
            isDefaultToPOS = Boolean.parseBoolean(request.getParameter("isDefaultToPOS"));
        }
        isIncludingGst = StringUtil.isNullOrEmpty("isIncludingGst")?false:Boolean.parseBoolean(request.getParameter("isIncludingGst"));
        boolean isCopy = StringUtil.isNullOrEmpty("isCopy") ? false : Boolean.parseBoolean(request.getParameter("isCopy"));
        
        HashMap requestParam = AccountingManager.getGlobalParams(request);
        if (!isCopy) {
            requestParam.put("id", itemID);
        }
        
        requestParam.put("name", request.getParameter("name"));
        requestParam.put("isDefaultToPOS", isDefaultToPOS);
        requestParam.put("isIncludingGst", isIncludingGst);

        HashMap<String, Object> filterRequestParams = new HashMap<String, Object>();
        filterRequestParams.put("name", request.getParameter("name"));
        filterRequestParams.put("companyid", sessionHandlerImpl.getCompanyid(request));
        
        KwlReturnObject cntResult = accMasterItemsDAOobj.getPricingBandItems(filterRequestParams);
        int count = cntResult.getRecordTotalCount();

        if (count == 1) {
            String recordID = ((PricingBandMaster) cntResult.getEntityList().get(0)).getID();
            isPresent = (itemID.equals(recordID) && !isCopy) ? false : true; // Allow Editing same record
        } else if (count > 1) {
            isPresent = true;
        }

        if (isPresent) {
            msg = "Price List - Band item entry for <b>" + request.getParameter("name") + "</b> already exists.";
            return new KwlReturnObject(false, msg, null, null, 0);
        } else {
            // For reset previously assigned isDefaultToPOS 
            if (isDefaultToPOS) {
                filterRequestParams = new HashMap<String, Object>();
                filterRequestParams.put("isDefaultToPOS", isDefaultToPOS);
                filterRequestParams.put("companyid", sessionHandlerImpl.getCompanyid(request));
                cntResult = accMasterItemsDAOobj.getPricingBandItems(filterRequestParams);
                
                if (cntResult.getEntityList() != null && !cntResult.getEntityList().isEmpty()) {
                    PricingBandMaster pricingBandMaster = (PricingBandMaster) cntResult.getEntityList().get(0);
                    
                    HashMap resetRequestParam = AccountingManager.getGlobalParams(request);
                    resetRequestParam.put("id", pricingBandMaster.getID());
                    resetRequestParam.put("isDefaultToPOS", false);
                    resetRequestParam.put("companyid", sessionHandlerImpl.getCompanyid(request));
                    
                    accMasterItemsDAOobj.addPricingBandItem(resetRequestParam);
                }
            }
            
            result = accMasterItemsDAOobj.addPricingBandItem(requestParam);
            PricingBandMaster pricingBandMaster = (PricingBandMaster) result.getEntityList().get(0);
            
            // for copy band detials
            if (isCopy) {
                Map requestMap = new HashMap();
                requestMap.put("existingBandID", itemID);
                requestMap.put("newBandID", pricingBandMaster.getID());
                copyPricingBandDetails(requestMap);
            }
        }
        return new KwlReturnObject(true, msg, null, result.getEntityList(), 0);
    }

    public ModelAndView getPricingBandItems(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        try {
            jobj = getPricingBandItems(request);
            issuccess = true;
        } catch (SessionExpiredException ex) {
            msg = ex.getMessage();
            Logger.getLogger(accMasterItemsController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ServiceException ex) {
            msg = ex.getMessage();
            Logger.getLogger(accMasterItemsController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            msg = "accMasterItemsController.getLocationItems : " + ex.getMessage();
            Logger.getLogger(accMasterItemsController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accMasterItemsController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public JSONObject getPricingBandItems(HttpServletRequest request) throws ServiceException, SessionExpiredException {
        JSONObject jobj = new JSONObject();
        int totalCount=0;
        try {
            HashMap<String, Object> filterRequestParams = new HashMap<String, Object>();
            filterRequestParams.put("companyid", sessionHandlerImpl.getCompanyid(request));
            if ((request.getParameter("start") != null) && (request.getParameter("limit") != null)) {
                filterRequestParams.put("start", request.getParameter("start"));
                filterRequestParams.put("limit", request.getParameter("limit"));
            }
            KwlReturnObject result = accMasterItemsDAOobj.getPricingBandItems(filterRequestParams);

            List list = result.getEntityList();
            totalCount=result.getRecordTotalCount();
            Iterator itr = list.iterator();
            JSONArray jArr = new JSONArray();
            while (itr.hasNext()) {
                PricingBandMaster pricingBandMaster = (PricingBandMaster) itr.next();
                JSONObject obj = new JSONObject();
                obj.put("id", pricingBandMaster.getID());
                obj.put("name", pricingBandMaster.getName());
                obj.put("isDefaultToPOS", pricingBandMaster.isDefaultToPOS());
                obj.put("isincludinggst", pricingBandMaster.isIsIncludingGST());
                jArr.put(obj);
            }
            jobj.put("data", jArr);
            jobj.put("totalCount", totalCount);
            
        } catch (JSONException e) {
            throw ServiceException.FAILURE(e.getMessage(), e);
        }
        return jobj;
    }
    
    public ModelAndView exportPricingBandMasterDetails(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String view = "jsonView_ex";
        try {
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            requestParams.put("companyID", sessionHandlerImpl.getCompanyid(request));
            if (!StringUtil.isNullOrEmpty(request.getParameter(Constants.ss))) {
                requestParams.put(Constants.ss, request.getParameter(Constants.ss));
            }
            request.setAttribute(Constants.IS_PRICE_LIST_BAND_REPORT, true);
            KwlReturnObject result = accMasterItemsDAOobj.getProductsForPricingBandMasterDetails(requestParams);
            
            List<Object[]> list = result.getEntityList();
            JSONArray DataJArr = getPricingBandMasterDetailsJson(request, list);
            jobj.put("data", DataJArr);

            String fileType = request.getParameter("filetype");
            if (StringUtil.equal(fileType, "print")) {
                String GenerateDate = authHandler.getDateFormatter(request).format(new Date());
                jobj.put("GenerateDate", GenerateDate);
                view = "jsonView-empty";
            }
            exportDaoObj.processRequest(request, response, jobj);
            jobj.put("success", true);
        } catch (SessionExpiredException ex) {
            Logger.getLogger(accMasterItemsController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(accMasterItemsController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new ModelAndView(view, "model", jobj.toString());
    }
    
    public ModelAndView getPricingbandMappedwithvolumeDisc(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        String volumediscountid = request.getParameter("pricingBandMasterID");
        try {

            String priceBands = accMasterItemsDAOobj.getPricingbandMappedwithvolumeDisc(volumediscountid);
            jobj.put("pricelistbandname", priceBands);
        } catch (Exception ex) {
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
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    
    public ModelAndView savePricingbandMappedwithvolumeDisc(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        String pricingbandmasterid = request.getParameter("pricingBandMasterID");
        try {
            if (pricingbandmasterid != null && !StringUtil.isNullOrEmpty(request.getParameter("pricelistbandmapping"))) {
                if (!StringUtil.isNullOrEmpty(pricingbandmasterid)) {
                    accMasterItemsDAOobj.deletePricingListBandMapping(pricingbandmasterid);
                }
                Map<String, Object> volParams = new HashMap<>();
                String[] pricelistbandmapping = request.getParameter("pricelistbandmapping").split(",");
                if (pricelistbandmapping != null) {
                    for (int j = 0; j < pricelistbandmapping.length; j++) {
                        volParams.put("volumediscountid", pricingbandmasterid);
                        volParams.put("pricingBandID", pricelistbandmapping[j]);

                        if (!StringUtil.isNullOrEmpty(pricingbandmasterid) && !StringUtil.isNullOrEmpty(pricelistbandmapping[j])) {
                            accMasterItemsDAOobj.savePricingListBandMapping(volParams);
                            issuccess=true;
                        }
                    }
                }
            }
        } catch (Exception ex) {
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
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    
    public ModelAndView getPricingBandMasterDetails(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        JSONArray DataJArr = new JSONArray();
        boolean issuccess = false;
        String msg = "";
        try {
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            requestParams.put("ss", request.getParameter("ss"));
            requestParams.put("start", request.getParameter("start"));
            requestParams.put("limit", request.getParameter("limit"));
            requestParams.put("companyID", sessionHandlerImpl.getCompanyid(request));
            request.setAttribute(Constants.IS_PRICE_LIST_BAND_REPORT, true);
            KwlReturnObject result = accMasterItemsDAOobj.getProductsForPricingBandMasterDetails(requestParams);
            
            List<Object[]> list = result.getEntityList();
            DataJArr = getPricingBandMasterDetailsJson(request, list);
            int totalCount = result.getRecordTotalCount();
            
            jobj.put("data", DataJArr);
            jobj.put("totalCount", totalCount);
            issuccess = true;
        } catch (SessionExpiredException ex) {
            msg = ex.getMessage();
            Logger.getLogger(accMasterItemsController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ServiceException ex) {
            msg = ex.getMessage();
            Logger.getLogger(accMasterItemsController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
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
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public JSONArray getPricingBandMasterDetailsJson(HttpServletRequest request, List<Object[]> list) throws JSONException, ServiceException {
        JSONArray jArr = new JSONArray();
        try {
            boolean isPricePolicyUseDiscount = StringUtil.isNullOrEmpty(request.getParameter("isPricePolicyUseDiscount")) ? false : Boolean.parseBoolean(request.getParameter("isPricePolicyUseDiscount"));
            boolean useCommonDiscount = StringUtil.isNullOrEmpty(request.getParameter("useCommonDiscount")) ? false : Boolean.parseBoolean(request.getParameter("useCommonDiscount"));
            String companyID = sessionHandlerImpl.getCompanyid(request);
            String currencyIDStr = request.getParameter("currencyID") != null? request.getParameter("currencyID") : "";
            String[] currencyIDArr = currencyIDStr.split(",");
            DateFormat df = authHandler.getDateOnlyFormat(request);
            String applicableDateStr = request.getParameter("applicableDate");
            Date applicableDate = null;
            double discountValue = 0;
            if (!StringUtil.isNullOrEmpty(applicableDateStr)) {
                applicableDate = df.parse(applicableDateStr);
            }
            PricingBandMaster pricingBandMaster = null;
            if (!isPricePolicyUseDiscount) {
                KwlReturnObject pricingBandMasterResult = accountingHandlerDAOobj.getObject(PricingBandMaster.class.getName(), request.getParameter("pricingBandMasterID"));
                pricingBandMaster = (PricingBandMaster) pricingBandMasterResult.getEntityList().get(0);
            }
            boolean isPriceListBandReport = false;
            if (request.getAttribute(Constants.IS_PRICE_LIST_BAND_REPORT) != null) {
                isPriceListBandReport = Boolean.parseBoolean(request.getAttribute(Constants.IS_PRICE_LIST_BAND_REPORT).toString());
            }
            HashMap<String, Object> requestParams = new HashMap<>();
            for (Object[] row : list) {
                String productUUID = (row[0] != null)? (String) row[0] : "";
                String productName = (row[1] != null)? (String) row[1] : "";
                String productID = (row[2] != null)? (String) row[2] : "";
                
                JSONObject obj = new JSONObject();
                obj.put("productUUID", productUUID);
                obj.put("productName", productName);
                obj.put("productID", productID);
                
                if (isPricePolicyUseDiscount) {
                    for (int i=0; i<currencyIDArr.length; i++) {
                        HashMap<String, Object> dataMap = new HashMap<String, Object>();
                        dataMap.put("isPricePolicyUseDiscount", isPricePolicyUseDiscount);
                        dataMap.put("useCommonDiscount", useCommonDiscount);
                        dataMap.put("pricingBandMasterID", request.getParameter("pricingBandMasterID"));
                        dataMap.put("applicableDate", applicableDate);
                        if (!useCommonDiscount) {
                            dataMap.put("productID", productUUID);
                        }
                        dataMap.put("companyID", companyID);
                        dataMap.put("currencyID", currencyIDArr[i]);
                        
                        KwlReturnObject result = accMasterItemsDAOobj.getPriceOfProductForPricingBandAndCurrency(dataMap);
                        
                        if (result.getEntityList() != null && !result.getEntityList().isEmpty()) {
                            String priceListVolumeDisocuntDetailID = result.getEntityList().get(0) != null ? (String) result.getEntityList().get(0) : "";

                            if (!StringUtil.isNullOrEmpty(priceListVolumeDisocuntDetailID)) {
                                KwlReturnObject detailsResult = accountingHandlerDAOobj.getObject(PricingBandMasterDetail.class.getName(), priceListVolumeDisocuntDetailID);
                                PricingBandMasterDetail pricingBandMasterDetail = (PricingBandMasterDetail) detailsResult.getEntityList().get(0);

                                obj.put("minimumQty", pricingBandMasterDetail.getMinimumQty());
                                obj.put("maximumQty", pricingBandMasterDetail.getMaximumQty());
                                obj.put("discountType", pricingBandMasterDetail.getDiscountType() != null? pricingBandMasterDetail.getDiscountType() : 0);
                                discountValue = authHandler.round(pricingBandMasterDetail.getDiscountValue(), companyID);
                                if (currencyIDArr[i].equalsIgnoreCase("1")) {
                                    obj.put("disocuntValueUSD", discountValue);
                                } else if (currencyIDArr[i].equalsIgnoreCase("10")) {
                                    obj.put("disocuntValueCAD", discountValue);
                                } else if (currencyIDArr[i].equalsIgnoreCase("11")) {
                                    obj.put("disocuntValueAUD", discountValue);
                                } else if (currencyIDArr[i].equalsIgnoreCase("12")) {
                                    obj.put("disocuntValueCNY", discountValue);
                                } else if (currencyIDArr[i].equalsIgnoreCase("13")) {
                                    obj.put("disocuntValueIDR", discountValue);
                                } else if (currencyIDArr[i].equalsIgnoreCase("14")) {
                                    obj.put("disocuntValueTWD", discountValue);
                                } else if (currencyIDArr[i].equalsIgnoreCase("15")) {
                                    obj.put("disocuntValueTHB", discountValue);
                                } else if (currencyIDArr[i].equalsIgnoreCase("16")) {
                                    obj.put("disocuntValuePHP", discountValue);
                                } else if (currencyIDArr[i].equalsIgnoreCase("17")) {
                                    obj.put("disocuntValueNZD", discountValue);
                                } else if (currencyIDArr[i].equalsIgnoreCase("18")) {
                                    obj.put("disocuntValueCHF", discountValue);
                                } else if (currencyIDArr[i].equalsIgnoreCase("2")) {
                                    obj.put("disocuntValueGBP", discountValue);
                                } else if (currencyIDArr[i].equalsIgnoreCase("3")) {
                                    obj.put("disocuntValueEUR", discountValue);
                                } else if (currencyIDArr[i].equalsIgnoreCase("5")) {
                                    obj.put("disocuntValueINR", discountValue);
                                } else if (currencyIDArr[i].equalsIgnoreCase("6")) {
                                    obj.put("disocuntValueSGD",  discountValue);
                                } else if (currencyIDArr[i].equalsIgnoreCase("7")) {
                                    obj.put("disocuntValueMYR", discountValue);
                                } else if (currencyIDArr[i].equalsIgnoreCase("8")) {
                                    obj.put("disocuntValueCRC", discountValue);
                                } else if (currencyIDArr[i].equalsIgnoreCase("9")) {
                                    obj.put("disocuntValueUGX", discountValue);
                                }else if (currencyIDArr[i].equalsIgnoreCase("19")) {
                                    obj.put("disocuntValueKRW", discountValue);
                                } else if (currencyIDArr[i].equalsIgnoreCase("20")) {
                                    obj.put("disocuntValueAED", discountValue);
                                } else if (currencyIDArr[i].equalsIgnoreCase("21")) {
                                    obj.put("disocuntValueBND", discountValue);
                                } else if (currencyIDArr[i].equalsIgnoreCase("22")) {
                                    obj.put("disocuntValueHKD", discountValue);
                                } else if (currencyIDArr[i].equalsIgnoreCase("23")) {
                                    obj.put("disocuntValueJPY", discountValue);
                                } else if (currencyIDArr[i].equalsIgnoreCase("24")) {
                                    obj.put("disocuntValueVND", discountValue);
                                } else if (currencyIDArr[i].equalsIgnoreCase("25")) {
                                    obj.put("disocuntValueOMR", discountValue);
                                } else if (currencyIDArr[i].equalsIgnoreCase("26")) {
                                    obj.put("disocuntValueCNH", discountValue);
                                }
                            }
                        }
                    }
                } else {
                    for (int i = 0; i < currencyIDArr.length; i++) {
                        requestParams.clear();
                        requestParams.put("pricingBandMasterID", request.getParameter("pricingBandMasterID"));
                        requestParams.put("applicableDate", applicableDate);
                        requestParams.put("currencyID", currencyIDArr[i]);
                        requestParams.put("productID", productUUID);
                        requestParams.put("companyID", companyID);
                        requestParams.put(Constants.IS_PRICE_LIST_BAND_REPORT, isPriceListBandReport);
                        KwlReturnObject result = accMasterItemsDAOobj.getPriceOfProductForPricingBandAndCurrency(requestParams);

                        if (result.getEntityList() != null && !result.getEntityList().isEmpty()) {
                            Object[] priceObj = (Object[]) result.getEntityList().get(0);

                            obj.put("minimumQty", priceObj[3] != null? (Integer) priceObj[3] : 0);
                            obj.put("maximumQty", priceObj[4] != null? (Integer) priceObj[4] : 0);
                            obj.put("pricingBandMasterName", pricingBandMaster != null ? pricingBandMaster.getName() : "");
                            
                            JSONObject requestParamsJson = new JSONObject();
                            requestParamsJson.put("productId", productUUID);
                            requestParamsJson.put("pricingBandMasterId", request.getParameter("pricingBandMasterID"));
                            requestParamsJson.put("companyid", companyID);
                            requestParamsJson.put("applicableDate", applicableDate);
                            requestParamsJson.put("currencyId", currencyIDArr[i]);
                            KwlReturnObject resultOfDiscount = accMasterItemsDAOobj.getDiscountOfProductForPricingBand(requestParamsJson);
                            List<Object[]> listOfDiscount = resultOfDiscount.getEntityList();
                            String dmid = "";
                            Iterator itr = listOfDiscount.iterator();
                            while (itr.hasNext()) {
                                String id=(String)itr.next();
                                dmid += !StringUtil.isNullOrEmpty(id) ? id + ',' : "";
                            }
                            dmid = !StringUtil.isNullOrEmpty(dmid) ? dmid.substring(0, dmid.length() - 1) : "";

                            if (currencyIDArr[i].equalsIgnoreCase("1")) {
                                obj.put("discountmasterUSD", dmid);
                                obj.put("purchasePriceUSD", priceObj[0] != null? (Double) priceObj[0] : 0);
                                obj.put("salesPriceUSD", priceObj[1] != null? (Double) priceObj[1] : 0);
                            } else if (currencyIDArr[i].equalsIgnoreCase("10")) {
                                obj.put("discountmasterCAD", dmid);
                                obj.put("purchasePriceCAD", priceObj[0] != null? (Double) priceObj[0] : 0);
                                obj.put("salesPriceCAD", priceObj[1] != null? (Double) priceObj[1] : 0);
                            } else if (currencyIDArr[i].equalsIgnoreCase("11")) {
                                obj.put("discountmasterAUD", dmid);
                                obj.put("purchasePriceAUD", priceObj[0] != null? (Double) priceObj[0] : 0);
                                obj.put("salesPriceAUD", priceObj[1] != null? (Double) priceObj[1] : 0);
                            } else if (currencyIDArr[i].equalsIgnoreCase("12")) {
                                obj.put("discountmasterCNY", dmid);
                                obj.put("purchasePriceCNY", priceObj[0] != null? (Double) priceObj[0] : 0);
                                obj.put("salesPriceCNY", priceObj[1] != null? (Double) priceObj[1] : 0);
                            } else if (currencyIDArr[i].equalsIgnoreCase("13")) {
                                obj.put("discountmasterIDR", dmid);
                                obj.put("purchasePriceIDR", priceObj[0] != null? (Double) priceObj[0] : 0);
                                obj.put("salesPriceIDR", priceObj[1] != null? (Double) priceObj[1] : 0);
                            } else if (currencyIDArr[i].equalsIgnoreCase("14")) {
                                obj.put("discountmasterTWD", dmid);
                                obj.put("purchasePriceTWD", priceObj[0] != null? (Double) priceObj[0] : 0);
                                obj.put("salesPriceTWD", priceObj[1] != null? (Double) priceObj[1] : 0);
                            } else if (currencyIDArr[i].equalsIgnoreCase("15")) {
                                obj.put("discountmasterTHB", dmid);
                                obj.put("purchasePriceTHB", priceObj[0] != null? (Double) priceObj[0] : 0);
                                obj.put("salesPriceTHB", priceObj[1] != null? (Double) priceObj[1] : 0);
                            } else if (currencyIDArr[i].equalsIgnoreCase("16")) {
                                obj.put("discountmasterPHP", dmid);
                                obj.put("purchasePricePHP", priceObj[0] != null? (Double) priceObj[0] : 0);
                                obj.put("salesPricePHP", priceObj[1] != null? (Double) priceObj[1] : 0);
                            } else if (currencyIDArr[i].equalsIgnoreCase("17")) {
                                obj.put("discountmasterNZD", dmid);
                                obj.put("purchasePriceNZD", priceObj[0] != null? (Double) priceObj[0] : 0);
                                obj.put("salesPriceNZD", priceObj[1] != null? (Double) priceObj[1] : 0);
                            } else if (currencyIDArr[i].equalsIgnoreCase("18")) {
                                obj.put("discountmasterCHF", dmid);
                                obj.put("purchasePriceCHF", priceObj[0] != null? (Double) priceObj[0] : 0);
                                obj.put("salesPriceCHF", priceObj[1] != null? (Double) priceObj[1] : 0);
                            } else if (currencyIDArr[i].equalsIgnoreCase("2")) {
                                obj.put("discountmasterGBP", dmid);
                                obj.put("purchasePriceGBP", priceObj[0] != null? (Double) priceObj[0] : 0);
                                obj.put("salesPriceGBP", priceObj[1] != null? (Double) priceObj[1] : 0);
                            } else if (currencyIDArr[i].equalsIgnoreCase("3")) {
                                obj.put("discountmasterEUR", dmid);
                                obj.put("purchasePriceEUR", priceObj[0] != null? (Double) priceObj[0] : 0);
                                obj.put("salesPriceEUR", priceObj[1] != null? (Double) priceObj[1] : 0);
                            } else if (currencyIDArr[i].equalsIgnoreCase("5")) {
                                obj.put("discountmasterINR", dmid);
                                obj.put("purchasePriceINR", priceObj[0] != null? (Double) priceObj[0] : 0);
                                obj.put("salesPriceINR", priceObj[1] != null? (Double) priceObj[1] : 0);
                            } else if (currencyIDArr[i].equalsIgnoreCase("6")) {
                                obj.put("discountmasterSGD", dmid);
                                obj.put("purchasePriceSGD", priceObj[0] != null? (Double) priceObj[0] : 0);
                                obj.put("salesPriceSGD", priceObj[1] != null? (Double) priceObj[1] : 0);
                            } else if (currencyIDArr[i].equalsIgnoreCase("7")) {
                                obj.put("discountmasterMYR", dmid);
                                obj.put("purchasePriceMYR", priceObj[0] != null? (Double) priceObj[0] : 0);
                                obj.put("salesPriceMYR", priceObj[1] != null? (Double) priceObj[1] : 0);
                            } else if (currencyIDArr[i].equalsIgnoreCase("8")) {
                                obj.put("discountmasterCRC", dmid);
                                obj.put("purchasePriceCRC", priceObj[0] != null? (Double) priceObj[0] : 0);
                                obj.put("salesPriceCRC", priceObj[1] != null? (Double) priceObj[1] : 0);
                            } else if (currencyIDArr[i].equalsIgnoreCase("9")) {
                                obj.put("discountmasterUGX", dmid);
                                obj.put("purchasePriceUGX", priceObj[0] != null? (Double) priceObj[0] : 0);
                                obj.put("salesPriceUGX", priceObj[1] != null? (Double) priceObj[1] : 0);
                            }else if (currencyIDArr[i].equalsIgnoreCase("19")) {
                                obj.put("discountmasterKRW", dmid);
                                obj.put("purchasePriceKRW", priceObj[0] != null ? (Double) priceObj[0] : 0);
                                obj.put("salesPriceKRW", priceObj[1] != null ? (Double) priceObj[1] : 0);
                            } else if (currencyIDArr[i].equalsIgnoreCase("20")) {
                                obj.put("discountmasterAED", dmid);
                                obj.put("purchasePriceAED", priceObj[0] != null ? (Double) priceObj[0] : 0);
                                obj.put("salesPriceAED", priceObj[1] != null ? (Double) priceObj[1] : 0);
                            } else if (currencyIDArr[i].equalsIgnoreCase("21")) {
                                obj.put("discountmasterBND", dmid);
                                obj.put("purchasePriceBND", priceObj[0] != null ? (Double) priceObj[0] : 0);
                                obj.put("salesPriceBND", priceObj[1] != null ? (Double) priceObj[1] : 0);
                            } else if (currencyIDArr[i].equalsIgnoreCase("22")) {
                                obj.put("discountmasterHKD", dmid);
                                obj.put("purchasePriceHKD", priceObj[0] != null ? (Double) priceObj[0] : 0);
                                obj.put("salesPriceHKD", priceObj[1] != null ? (Double) priceObj[1] : 0);
                            } else if (currencyIDArr[i].equalsIgnoreCase("23")) {
                                obj.put("discountmasterJPY", dmid);
                                obj.put("purchasePriceJPY", priceObj[0] != null ? (Double) priceObj[0] : 0);
                                obj.put("salesPriceJPY", priceObj[1] != null ? (Double) priceObj[1] : 0);
                            } else if (currencyIDArr[i].equalsIgnoreCase("24")) {
                                obj.put("discountmasterVND", dmid);
                                obj.put("purchasePriceVND", priceObj[0] != null ? (Double) priceObj[0] : 0);
                                obj.put("salesPriceVND", priceObj[1] != null ? (Double) priceObj[1] : 0);
                            } else if (currencyIDArr[i].equalsIgnoreCase("25")) {
                                obj.put("discountmasterOMR", dmid);
                                obj.put("purchasePriceOMR", priceObj[0] != null ? (Double) priceObj[0] : 0);
                                obj.put("salesPriceOMR", priceObj[1] != null ? (Double) priceObj[1] : 0);
                            } else if (currencyIDArr[i].equalsIgnoreCase("26")) {
                                obj.put("discountmasterCNH", dmid);
                                obj.put("purchasePriceCNH", priceObj[0] != null ? (Double) priceObj[0] : 0);
                                obj.put("salesPriceCNH", priceObj[1] != null ? (Double) priceObj[1] : 0);
                            } else if (currencyIDArr[i].equalsIgnoreCase("27")) {
                                obj.put("discountmasterSAR", dmid);
                                obj.put("purchasePriceSAR", priceObj[0] != null ? (Double) priceObj[0] : 0);
                                obj.put("salesPriceSAR", priceObj[1] != null ? (Double) priceObj[1] : 0);
                            } else if (currencyIDArr[i].equalsIgnoreCase("28")) {
                                obj.put("discountmasterZAR", dmid);
                                obj.put("purchasePriceZAR", priceObj[0] != null ? (Double) priceObj[0] : 0);
                                obj.put("salesPriceZAR", priceObj[1] != null ? (Double) priceObj[1] : 0);
                            } else if (currencyIDArr[i].equalsIgnoreCase("29")) {
                                obj.put("discountmasterSEK", dmid);
                                obj.put("purchasePriceSEK", priceObj[0] != null ? (Double) priceObj[0] : 0);
                                obj.put("salesPriceSEK", priceObj[1] != null ? (Double) priceObj[1] : 0);
                            } else if (currencyIDArr[i].equalsIgnoreCase("30")) {
                                obj.put("discountmasterBDT", dmid);
                                obj.put("purchasePriceBDT", priceObj[0] != null ? (Double) priceObj[0] : 0);
                                obj.put("salesPriceBDT", priceObj[1] != null ? (Double) priceObj[1] : 0);
                            } else if (currencyIDArr[i].equalsIgnoreCase("31")) {
                                obj.put("discountmasterMMK", dmid);
                                obj.put("purchasePriceMMK", priceObj[0] != null ? (Double) priceObj[0] : 0);
                                obj.put("salesPriceMMK", priceObj[1] != null ? (Double) priceObj[1] : 0);
                            } else if (currencyIDArr[i].equalsIgnoreCase("32")) {
                                obj.put("discountmasterNGN", dmid);
                                obj.put("purchasePriceNGN", priceObj[0] != null ? (Double) priceObj[0] : 0);
                                obj.put("salesPriceNGN", priceObj[1] != null ? (Double) priceObj[1] : 0);
                            }
                        }

                    }
                }
                
                jArr.put(obj);
            }
        } catch (Exception ex) {
            Logger.getLogger(accMasterItemsController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return jArr;
    }
    
    
    public ModelAndView getPricingBandMasterProductDetails(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        JSONArray DataJArr = new JSONArray();
        boolean issuccess = false;
        String msg = "";
        try {
            boolean isFlatPriceListVolumeDiscount = StringUtil.isNullOrEmpty(request.getParameter("isFlatPriceListVolumeDiscount")) ? false : Boolean.parseBoolean(request.getParameter("isFlatPriceListVolumeDiscount"));
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            requestParams.put("ss", request.getParameter("ss"));
            requestParams.put("start", request.getParameter("start"));
            requestParams.put("limit", request.getParameter("limit"));
            requestParams.put("companyid", sessionHandlerImpl.getCompanyid(request));

            KwlReturnObject result;
            if (isFlatPriceListVolumeDiscount) {
                requestParams.put("isFlatPriceListVolumeDiscount", isFlatPriceListVolumeDiscount);
                
                result = accMasterItemsDAOobj.getPriceListVolumeDiscountItems(requestParams);
            } else {
                result = accMasterItemsDAOobj.getPricingBandItems(requestParams);
            }

            List<PricingBandMaster> list = result.getEntityList();
            DataJArr = getPricingBandMasterProductDetailsJson(request, list);
            int totalCount = result.getRecordTotalCount();

            jobj.put("data", DataJArr);
            jobj.put("totalCount", totalCount);
            issuccess = true;
        } catch (SessionExpiredException ex) {
            msg = ex.getMessage();
            Logger.getLogger(accMasterItemsController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ServiceException ex) {
            msg = ex.getMessage();
            Logger.getLogger(accMasterItemsController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
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
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public JSONArray getPricingBandMasterProductDetailsJson(HttpServletRequest request, List<PricingBandMaster> list) throws JSONException, ServiceException {
        JSONArray jArr = new JSONArray();
        try {
            boolean isFlatPriceListVolumeDiscount = StringUtil.isNullOrEmpty(request.getParameter("isFlatPriceListVolumeDiscount")) ? false : Boolean.parseBoolean(request.getParameter("isFlatPriceListVolumeDiscount"));
            String companyID = sessionHandlerImpl.getCompanyid(request);
            String currencyIDStr = request.getParameter("currencyID") != null ? request.getParameter("currencyID") : "";
            String[] currencyIDArr = currencyIDStr.split(",");
//            DateFormat df = authHandler.getDateFormatter(request);    //refer ticket ERP-15117
            DateFormat df = authHandler.getDateOnlyFormat(request);
            String applicableDateStr = request.getParameter("applicableDate");
            Date applicableDate = null;
            if (!StringUtil.isNullOrEmpty(applicableDateStr)) {
                applicableDate = df.parse(applicableDateStr);
            }

            for (PricingBandMaster row : list) {
                JSONObject obj = new JSONObject();
                
                obj.put("bandUUID", row.getID());
                obj.put("bandName", row.getName());
                if (isFlatPriceListVolumeDiscount) {
                    obj.put("desc", row.getDescription());
                }
                
                for (int i = 0; i < currencyIDArr.length; i++) {
                    HashMap<String, Object> requestParams = new HashMap<String, Object>();
                    requestParams.put("pricingBandMasterID", row.getID());
                    requestParams.put("applicableDate", applicableDate);
                    requestParams.put("currencyID", currencyIDArr[i]);
                    requestParams.put("productID", request.getParameter("productID"));
                    requestParams.put("companyID", companyID);
                    KwlReturnObject result = accMasterItemsDAOobj.getPriceOfBandForProductAndCurrency(requestParams);

                    if (result.getEntityList() != null && !result.getEntityList().isEmpty()) {
                        Object[] priceObj = (Object[]) result.getEntityList().get(0);
                        
                        if (isFlatPriceListVolumeDiscount) {
                            obj.put("minimumQty", priceObj[3] != null? (Integer) priceObj[3] : 0);
                            obj.put("maximumQty", priceObj[4] != null? (Integer) priceObj[4] : 0);
                        }

                        if (currencyIDArr[i].equalsIgnoreCase("1")) {
                            obj.put("purchasePriceUSD", priceObj[0] != null ? (Double) priceObj[0] : 0);
                            obj.put("salesPriceUSD", priceObj[1] != null ? (Double) priceObj[1] : 0);
                        } else if (currencyIDArr[i].equalsIgnoreCase("10")) {
                            obj.put("purchasePriceCAD", priceObj[0] != null ? (Double) priceObj[0] : 0);
                            obj.put("salesPriceCAD", priceObj[1] != null ? (Double) priceObj[1] : 0);
                        } else if (currencyIDArr[i].equalsIgnoreCase("11")) {
                            obj.put("purchasePriceAUD", priceObj[0] != null ? (Double) priceObj[0] : 0);
                            obj.put("salesPriceAUD", priceObj[1] != null ? (Double) priceObj[1] : 0);
                        } else if (currencyIDArr[i].equalsIgnoreCase("12")) {
                            obj.put("purchasePriceCNY", priceObj[0] != null ? (Double) priceObj[0] : 0);
                            obj.put("salesPriceCNY", priceObj[1] != null ? (Double) priceObj[1] : 0);
                        } else if (currencyIDArr[i].equalsIgnoreCase("13")) {
                            obj.put("purchasePriceIDR", priceObj[0] != null ? (Double) priceObj[0] : 0);
                            obj.put("salesPriceIDR", priceObj[1] != null ? (Double) priceObj[1] : 0);
                        } else if (currencyIDArr[i].equalsIgnoreCase("14")) {
                            obj.put("purchasePriceTWD", priceObj[0] != null ? (Double) priceObj[0] : 0);
                            obj.put("salesPriceTWD", priceObj[1] != null ? (Double) priceObj[1] : 0);
                        } else if (currencyIDArr[i].equalsIgnoreCase("15")) {
                            obj.put("purchasePriceTHB", priceObj[0] != null ? (Double) priceObj[0] : 0);
                            obj.put("salesPriceTHB", priceObj[1] != null ? (Double) priceObj[1] : 0);
                        } else if (currencyIDArr[i].equalsIgnoreCase("16")) {
                            obj.put("purchasePricePHP", priceObj[0] != null ? (Double) priceObj[0] : 0);
                            obj.put("salesPricePHP", priceObj[1] != null ? (Double) priceObj[1] : 0);
                        } else if (currencyIDArr[i].equalsIgnoreCase("17")) {
                            obj.put("purchasePriceNZD", priceObj[0] != null ? (Double) priceObj[0] : 0);
                            obj.put("salesPriceNZD", priceObj[1] != null ? (Double) priceObj[1] : 0);
                        } else if (currencyIDArr[i].equalsIgnoreCase("18")) {
                            obj.put("purchasePriceCHF", priceObj[0] != null ? (Double) priceObj[0] : 0);
                            obj.put("salesPriceCHF", priceObj[1] != null ? (Double) priceObj[1] : 0);
                        } else if (currencyIDArr[i].equalsIgnoreCase("2")) {
                            obj.put("purchasePriceGBP", priceObj[0] != null ? (Double) priceObj[0] : 0);
                            obj.put("salesPriceGBP", priceObj[1] != null ? (Double) priceObj[1] : 0);
                        } else if (currencyIDArr[i].equalsIgnoreCase("3")) {
                            obj.put("purchasePriceEUR", priceObj[0] != null ? (Double) priceObj[0] : 0);
                            obj.put("salesPriceEUR", priceObj[1] != null ? (Double) priceObj[1] : 0);
                        } else if (currencyIDArr[i].equalsIgnoreCase("5")) {
                            obj.put("purchasePriceINR", priceObj[0] != null ? (Double) priceObj[0] : 0);
                            obj.put("salesPriceINR", priceObj[1] != null ? (Double) priceObj[1] : 0);
                        } else if (currencyIDArr[i].equalsIgnoreCase("6")) {
                            obj.put("purchasePriceSGD", priceObj[0] != null ? (Double) priceObj[0] : 0);
                            obj.put("salesPriceSGD", priceObj[1] != null ? (Double) priceObj[1] : 0);
                        } else if (currencyIDArr[i].equalsIgnoreCase("7")) {
                            obj.put("purchasePriceMYR", priceObj[0] != null ? (Double) priceObj[0] : 0);
                            obj.put("salesPriceMYR", priceObj[1] != null ? (Double) priceObj[1] : 0);
                        } else if (currencyIDArr[i].equalsIgnoreCase("8")) {
                            obj.put("purchasePriceCRC", priceObj[0] != null ? (Double) priceObj[0] : 0);
                            obj.put("salesPriceCRC", priceObj[1] != null ? (Double) priceObj[1] : 0);
                        } else if (currencyIDArr[i].equalsIgnoreCase("9")) {
                            obj.put("purchasePriceUGX", priceObj[0] != null ? (Double) priceObj[0] : 0);
                            obj.put("salesPriceUGX", priceObj[1] != null ? (Double) priceObj[1] : 0);
                        } else if (currencyIDArr[i].equalsIgnoreCase("19")) {
                            obj.put("purchasePriceKRW", priceObj[0] != null ? (Double) priceObj[0] : 0);
                            obj.put("salesPriceKRW", priceObj[1] != null ? (Double) priceObj[1] : 0);
                        } else if (currencyIDArr[i].equalsIgnoreCase("20")) {
                            obj.put("purchasePriceAED", priceObj[0] != null ? (Double) priceObj[0] : 0);
                            obj.put("salesPriceAED", priceObj[1] != null ? (Double) priceObj[1] : 0);
                        } else if (currencyIDArr[i].equalsIgnoreCase("21")) {
                            obj.put("purchasePriceBND", priceObj[0] != null ? (Double) priceObj[0] : 0);
                            obj.put("salesPriceBND", priceObj[1] != null ? (Double) priceObj[1] : 0);
                        } else if (currencyIDArr[i].equalsIgnoreCase("22")) {
                            obj.put("purchasePriceHKD", priceObj[0] != null ? (Double) priceObj[0] : 0);
                            obj.put("salesPriceHKD", priceObj[1] != null ? (Double) priceObj[1] : 0);
                        } else if (currencyIDArr[i].equalsIgnoreCase("23")) {
                            obj.put("purchasePriceJPY", priceObj[0] != null ? (Double) priceObj[0] : 0);
                            obj.put("salesPriceJPY", priceObj[1] != null ? (Double) priceObj[1] : 0);
                        } else if (currencyIDArr[i].equalsIgnoreCase("24")) {
                            obj.put("purchasePriceVND", priceObj[0] != null ? (Double) priceObj[0] : 0);
                            obj.put("salesPriceVND", priceObj[1] != null ? (Double) priceObj[1] : 0);
                        } else if (currencyIDArr[i].equalsIgnoreCase("25")) {
                            obj.put("purchasePriceOMR", priceObj[0] != null ? (Double) priceObj[0] : 0);
                            obj.put("salesPriceOMR", priceObj[1] != null ? (Double) priceObj[1] : 0);
                        } else if (currencyIDArr[i].equalsIgnoreCase("26")) {
                            obj.put("purchasePriceCNH", priceObj[0] != null ? (Double) priceObj[0] : 0);
                            obj.put("salesPriceCNH", priceObj[1] != null ? (Double) priceObj[1] : 0);
                        }
                    }
                }
                
                jArr.put(obj);
            }
        } catch (Exception ex) {
            Logger.getLogger(accMasterItemsController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return jArr;
    }
    
    public ModelAndView savePricingBandMasterDetails(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = true, isCommitEx = false;

        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("Account_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        Session session =null;
        TransactionStatus status = txnManager.getTransaction(def);
        try {
//            DateFormat df = authHandler.getDateFormatter(request);    //refer ticket ERP-15117
            DateFormat df = authHandler.getDateOnlyFormat(request);
            String companyID = sessionHandlerImpl.getCompanyid(request);
            String companySubDomain = sessionHandlerImpl.getCompanySessionObj(request).getCdomain();
            String currencyID = getCurrencyIDFromIndex(request);
            String applicableDateStr = request.getParameter("applicableDate");
            Date applicableDate = null;
            if (!StringUtil.isNullOrEmpty(applicableDateStr)) {
                applicableDate = df.parse(applicableDateStr);
            }
            boolean isVolumeDiscount = StringUtil.isNullOrEmpty(request.getParameter("isVolumeDiscount"))? false : Boolean.parseBoolean(request.getParameter("isVolumeDiscount"));
            boolean isPricePolicyUseDiscount = StringUtil.isNullOrEmpty(request.getParameter("isPricePolicyUseDiscount"))? false : Boolean.parseBoolean(request.getParameter("isPricePolicyUseDiscount"));
            boolean useCommonDiscount = StringUtil.isNullOrEmpty(request.getParameter("useCommonDiscount"))? false : Boolean.parseBoolean(request.getParameter("useCommonDiscount"));
            boolean checkMinQty = StringUtil.isNullOrEmpty(request.getParameter("checkMinQty"))? false : Boolean.parseBoolean(request.getParameter("checkMinQty"));
            boolean isDiscountMasterUpdated = StringUtil.isNullOrEmpty(request.getParameter("isDiscountMasterUpdated"))? false : Boolean.parseBoolean(request.getParameter("isDiscountMasterUpdated"));
            
            String column_Name = request.getParameter("column_Name") == null ? "" : request.getParameter("column_Name");
            String column_Value = (request.getParameter("column_Value") == null || request.getParameter("column_Value") == "") ? "0" : request.getParameter("column_Value");
            
            int minimumQty = StringUtil.isNullOrEmpty(request.getParameter("minimumQty")) ? 0 : Integer.parseInt(request.getParameter("minimumQty"));
            int maximumQty = StringUtil.isNullOrEmpty(request.getParameter("maximumQty")) ? 0 : Integer.parseInt(request.getParameter("maximumQty"));
            String discountType = StringUtil.isNullOrEmpty(request.getParameter("discountType")) ? "" : request.getParameter("discountType");
            //pricebandIDS mapped with volume discount
            String pricebandidsmappedwithvol = StringUtil.isNullOrEmpty(request.getParameter("pricebandidsmappedwithvol")) ? "" : request.getParameter("pricebandidsmappedwithvol");
            
            /**
             * isPricePolicyUseDiscount: true for Use Discount and false for Use Flat price 
             * 
             */
            if (isPricePolicyUseDiscount) {
                HashMap<String, Object> dataMap = new HashMap<String, Object>();
                dataMap.put("useCommonDiscount", useCommonDiscount);
                dataMap.put("isPricePolicyUseDiscount", isPricePolicyUseDiscount);
                dataMap.put("pricingBandMasterID", request.getParameter("pricingBandMasterID"));//Volume discount id
                dataMap.put("applicableDate", applicableDate);
                dataMap.put("isSavePricingBandMasterDetails", true);
                if (!useCommonDiscount) {
                    dataMap.put("productID", request.getParameter("productID"));
                }
                dataMap.put("minimumQty", minimumQty);
                dataMap.put("maximumQty", maximumQty);
                dataMap.put("discountType", discountType);
                dataMap.put("companyID", companyID);
                dataMap.put("currencyID", currencyID);
                dataMap.put("pricebandidsmappedwithvol", pricebandidsmappedwithvol);//pricing band ids
                
                if (useCommonDiscount) {
                    updateCommonDisocuntForAllProducts(request);
                    
                    String currencyIDStr = request.getParameter("currency") != null ? request.getParameter("currency") : "";
                    String[] currencyIDArr = currencyIDStr.split(",");
                    for (int i = 0; i < currencyIDArr.length; i++) {
                        KwlReturnObject resultOfPrice = accMasterItemsDAOobj.getPriceOfProductForPricingBandAndCurrency(dataMap);
                        if (resultOfPrice.getEntityList() != null && !resultOfPrice.getEntityList().isEmpty()) { // for edit case
                            String rowid = (String) resultOfPrice.getEntityList().get(0);
                            dataMap.put("rowid", rowid);
                        }
                        dataMap.put("disocuntValue", request.getParameter("disocuntValue"));
                        dataMap.put("currencyID", currencyIDArr[i]);

                        accMasterItemsDAOobj.saveOrUpdatePricingBandMasterDetails(dataMap);
                    }
                    
                } else if (column_Name.contains("minimumQty") || column_Name.contains("maximumQty") || column_Name.contains("discountType")) {
                    String currencyIDStr = request.getParameter("currency") != null ? request.getParameter("currency") : "";
                    String[] currencyIDArr = currencyIDStr.split(",");

                    for (int i = 0; i < currencyIDArr.length; i++) {
                        dataMap.put("currencyID", currencyIDArr[i]);
                        List existingVolume=accMasterItemsDAOobj.getExistingVolumes(dataMap);
                        Iterator itr= existingVolume.iterator();
                        boolean isexists=false;
                        while (itr.hasNext()) {
                            String priceListVolumeDisocuntDetailID = (String) itr.next();
                            KwlReturnObject resultOfPrice = accMasterItemsDAOobj.getPriceOfProductForPricingBandAndCurrency(dataMap);
                            String rowid = "";
                            if (resultOfPrice.getEntityList() != null && !resultOfPrice.getEntityList().isEmpty()) { // for edit case
                                rowid = (String) resultOfPrice.getEntityList().get(0);
                                dataMap.put("rowid", rowid);
                            }
                            if (rowid.equals(priceListVolumeDisocuntDetailID)) {
                                continue;
                            }
                            KwlReturnObject detailsResult = accountingHandlerDAOobj.getObject(PricingBandMasterDetail.class.getName(), priceListVolumeDisocuntDetailID);
                            PricingBandMasterDetail pricingBandMasterDetail = (PricingBandMasterDetail) detailsResult.getEntityList().get(0);
                            int min = pricingBandMasterDetail.getMinimumQty();
                            int max = pricingBandMasterDetail.getMaximumQty();
                            
                            /**
                             * minimumQty: minimum entered qty 
                             * maximumQty:maximum entered qty 
                             * checkMinQty:flag true when user entered the minimum qty otherwise false
                             */
                            if (checkMinQty) {
                                /**
                                 * This block checks entered minimum qty is
                                 * present between min and max qty of existing
                                 * volume discount or not
                                 */
                                if (min <= minimumQty && minimumQty <= max) {
                                    isexists = true;
                                }
                                /**
                                 * checks the entered minimum qty is less than
                                 * maximum entered quantity
                                 */
                                if (minimumQty <= min && min <= maximumQty) {
                                    isexists = true;
                                }
                            } else {
                                /**
                                 * This block checks entered Maximum qty is
                                 * present between min and max qty of existing
                                 * volume discount or not
                                 */
                                if (min <= maximumQty && maximumQty <= max) {
                                    isexists = true;
                                }
                                if (minimumQty <= max && max <= maximumQty) {
                                    isexists = true;
                                }
                            }
                        }
                        if (isexists) {
                            issuccess = true;
                            msg = "Volume is already defined.";
                            jobj.put("success", issuccess);
                            jobj.put("msg", msg);
                            txnManager.rollback(status);
                            return new ModelAndView("jsonView", "model", jobj.toString());
                        }
                        KwlReturnObject resultOfPrice = accMasterItemsDAOobj.getPriceOfProductForPricingBandAndCurrency(dataMap);

                        if (resultOfPrice.getEntityList() != null && !resultOfPrice.getEntityList().isEmpty()) { // for edit case
                            List<String> priceList = resultOfPrice.getEntityList();
                            for (String priceVolObj : priceList) {
                                dataMap.put("rowid", priceVolObj);

                                accMasterItemsDAOobj.saveOrUpdatePricingBandMasterDetails(dataMap);
                                dataMap.remove("rowid");
                            }
                        } else {
                            accMasterItemsDAOobj.saveOrUpdatePricingBandMasterDetails(dataMap);
                        }
                    }
                } else {
                    KwlReturnObject resultOfPrice = accMasterItemsDAOobj.getPriceOfProductForPricingBandAndCurrency(dataMap);
                    
                    if (column_Name.contains("disocuntValue")) {
                        dataMap.put("disocuntValue", column_Value);
                    }
                    
                    if (resultOfPrice.getEntityList() != null && !resultOfPrice.getEntityList().isEmpty()) { // for edit case
                        String rowid = (String) resultOfPrice.getEntityList().get(0);
                        dataMap.put("rowid", rowid);
                    }
                    
                    accMasterItemsDAOobj.saveOrUpdatePricingBandMasterDetails(dataMap);
                }
            } else if (isDiscountMasterUpdated) {
                HashMap requestParam=new HashMap();
                String discountname="";
                String userName=sessionHandlerImpl.getUserName(request);
                String productName=request.getParameter("productName");
                String pricingBandMasterName=request.getParameter("pricingBandMasterName");
                if (column_Name.contains("discountmaster")) {
//                        Set discountMastersSet = new HashSet<>();
                    JSONObject paramObj = new JSONObject();
                    String[] discountMasterIds = column_Value.split(",");
                    String productid = request.getParameter("productID");
                    String pricingBandMasterId = request.getParameter("pricingBandMasterID");
                    paramObj.put("pricingBandMasterId", pricingBandMasterId);
                    paramObj.put("productId", productid);
                    paramObj.put("companyid", companyID);
                    paramObj.put("applicableDate", applicableDate);
                    paramObj.put("currencyId", currencyID);
                    paramObj.put("isFromSave", true);               //sending flag isFromSave true to identitfy that call is from save function
                    KwlReturnObject resultOfDiscount = accMasterItemsDAOobj.getDiscountOfProductForPricingBand(paramObj);       //For edit case
                    List<Object[]> listOfDiscount = resultOfDiscount.getEntityList();
                    if (listOfDiscount.size() > 0) {                                                                                //For edit case
                        accMasterItemsDAOobj.deleteProductDiscountMapping(paramObj);
                    }
                    if (!discountMasterIds[0].equals("0")) {                    //If all the discount applied to a product are removed then discountMasterIds contain 0 so deleteing all the discount mapping from the table
                        for (String id : discountMasterIds) {
                            paramObj.put("discountMasterId", id);
                            KwlReturnObject resultOfProductDiscountMapping = accMasterItemsDAOobj.setProductDiscountMapping(paramObj);
                            if (resultOfProductDiscountMapping.isSuccessFlag()) {
                                KwlReturnObject discountRetObj = accountingHandlerDAOobj.getObject(DiscountMaster.class.getName(), id);
                                DiscountMaster discountMaster = (DiscountMaster) discountRetObj.getEntityList().get(0);
                                if (discountMaster != null) {
                                    discountname = discountMaster.getName();
                                }
                                auditTrailObj.insertAuditLog(AuditAction.DISCOUNT_MASTER_ASSIGNED, "User " + userName + " has assigned discount " + discountname + " to " + productName + " product of " + pricingBandMasterName + " priceband applicable from " + applicableDateStr, request, id);
                            }
                        }
                    }else{
                        paramObj.put("isDeleted", true);
                        KwlReturnObject resultOfProductDiscountMapping = accMasterItemsDAOobj.setProductDiscountMapping(paramObj);
                            if (resultOfProductDiscountMapping.isSuccessFlag()) {
                                auditTrailObj.insertAuditLog(AuditAction.DISCOUNT_MASTER_ASSIGNED, "User " + userName + " has deleted all discounts applied to " + productName + " product of " + pricingBandMasterName + " priceband applicable from " + applicableDateStr, request,"");
                            }
                    }
                }
            } else {
                HashMap<String, Object> requestParams = new HashMap<String, Object>();
                requestParams.put("pricingBandMasterID", request.getParameter("pricingBandMasterID"));
                requestParams.put("applicableDate", applicableDate);
                requestParams.put("isSavePricingBandMasterDetails", true);
                requestParams.put("currencyID", currencyID);
                requestParams.put("productID", request.getParameter("productID"));
                requestParams.put("companyID", companyID);

                boolean isQtyChanged = false;
                if (isVolumeDiscount) {
                    requestParams.put("isVolumeDiscount", isVolumeDiscount);
                    requestParams.put("minimumQty", minimumQty);
                    requestParams.put("maximumQty", maximumQty);
                    requestParams.put("pricebandidsmappedwithvol", pricebandidsmappedwithvol);//pricing band ids

                    if (column_Name.contains("minimumQty") || column_Name.contains("maximumQty")) {
                        String currencyIDStr = request.getParameter("currency") != null ? request.getParameter("currency") : "";
                        String[] currencyIDArr = currencyIDStr.split(",");

                        for (int i = 0; i < currencyIDArr.length; i++) {
                            isQtyChanged = true;
                            requestParams.put("currencyID", currencyIDArr[i]);
                            List existingVolume = accMasterItemsDAOobj.getExistingVolumes(requestParams);
                            Iterator itr = existingVolume.iterator();
                            boolean isexists = false;
                            while (itr.hasNext()) {
                                String priceListVolumeDisocuntDetailID = (String) itr.next();
                                KwlReturnObject resultOfPrice = accMasterItemsDAOobj.getPriceOfProductForPricingBandAndCurrency(requestParams);
                                String rowid = "";
                                if (resultOfPrice.getEntityList() != null && !resultOfPrice.getEntityList().isEmpty()) { // for edit case
                                    List<Object[]> priceList = resultOfPrice.getEntityList();
                                    for (Object[] priceVolObj : priceList) {
                                        requestParams.put("rowid", priceVolObj[2]);
                                        rowid = (String) priceVolObj[2];
                                    }
                                    requestParams.put("rowid", rowid);
                                }
                                if (rowid.equals(priceListVolumeDisocuntDetailID)) {
                                    continue;
                                }
                                KwlReturnObject detailsResult = accountingHandlerDAOobj.getObject(PricingBandMasterDetail.class.getName(), priceListVolumeDisocuntDetailID);
                                PricingBandMasterDetail pricingBandMasterDetail = (PricingBandMasterDetail) detailsResult.getEntityList().get(0);
                                int min = pricingBandMasterDetail.getMinimumQty();
                                int max = pricingBandMasterDetail.getMaximumQty();

                                /**
                                 * minimumQty: minimum entered qty maximumQty:
                                 * maximum entered qty checkMinQty:flag true
                                 * when user entered the minimum qty otherwise
                                 * false
                                 */
                                if (checkMinQty) {
                                    /**
                                     * This block checks entered minimum qty is
                                     * present between min and max qty of
                                     * existing volume discount or not
                                     */
                                    if (min <= minimumQty && minimumQty <= max) {
                                        isexists = true;
                                    }
                                    /**
                                     * checks the entered minimum qty is less
                                     * than maximum entered quantity
                                     */
                                    if (minimumQty <= min && min <= maximumQty) {
                                        isexists = true;
                                    }
                                } else {
                                    /**
                                     * This block checks entered Maximum qty is
                                     * present between min and max qty of
                                     * existing volume discount or not
                                     */
                                    if (min <= maximumQty && maximumQty <= max) {
                                        isexists = true;
                                    }
                                    if (minimumQty <= max && max <= maximumQty) {
                                        isexists = true;
                                    }
                                }
                            }
                            if (isexists) {
                                issuccess = true;
                                msg = "Volume is already defined.";
                                jobj.put("success", issuccess);
                                jobj.put("msg", msg);
                                txnManager.rollback(status);
                                return new ModelAndView("jsonView", "model", jobj.toString());
                            }

                            KwlReturnObject resultOfPrice = accMasterItemsDAOobj.getPriceOfProductForPricingBandAndCurrency(requestParams);

                            if (resultOfPrice.getEntityList() != null && !resultOfPrice.getEntityList().isEmpty()) { // for edit case
                                List<Object[]> priceList = resultOfPrice.getEntityList();
                                for (Object[] priceVolObj : priceList) {
                                    requestParams.put("rowid", priceVolObj[2]);

                                    accMasterItemsDAOobj.saveOrUpdatePricingBandMasterDetails(requestParams);
                                    requestParams.remove("rowid");
                                }
                            } else {
                                accMasterItemsDAOobj.saveOrUpdatePricingBandMasterDetails(requestParams);
                            }
                        }
                    }
                }

                if (!isQtyChanged) {
                    KwlReturnObject result = accMasterItemsDAOobj.getPriceOfProductForPricingBandAndCurrency(requestParams);

                    Object[] priceObj = null;
                    String salesPrice ="0.0",purchasePrice="0.0";
                    
                    if(result.getEntityList() != null && !result.getEntityList().isEmpty()){
                          priceObj=(Object[]) result.getEntityList().get(0);
                          salesPrice=priceObj[1].toString();
                          purchasePrice=priceObj[0].toString();
                    }
                    
                    if (column_Name.contains("purchasePrice")) {
                        requestParams.put("purchasePrice", column_Value);
                        requestParams.put("salesPrice",salesPrice);
                    } else {
                        requestParams.put("salesPrice", column_Value);
                        requestParams.put("purchasePrice",purchasePrice);
                    }

                    if (result.getEntityList() != null && !result.getEntityList().isEmpty()) { // for edit case
                        requestParams.put("rowid", priceObj[2]);
                    }

                    result = accMasterItemsDAOobj.saveOrUpdatePricingBandMasterDetails(requestParams);

                    // to sync price to the pos
                    if (result.getEntityList() != null && !result.getEntityList().isEmpty()) {
                        PricingBandMasterDetail pricingBandMasterDetail = (PricingBandMasterDetail) result.getEntityList().get(0);

//                        if (pricingBandMasterDetail.getPricingBandMaster() != null && pricingBandMasterDetail.getPricingBandMaster().isDefaultToPOS()) {
//                            session = HibernateUtil.getCurrentSession();
//                            String posURL = this.getServletContext().getInitParameter("posURL");
//                            String action = "33";
//
//                            JSONObject userData = new JSONObject();
//                            userData.put("iscommit", true);
//                            userData.put("remoteapikey", StorageHandler.GetRemoteAPIKey());
//                            userData.put("userid", sessionHandlerImpl.getUserid(request));
//                            userData.put("companyid", companyID);
//                            userData.put("subdomain", companySubDomain);
//                            userData.put("action", action);
//
//                            JSONArray jArr = new JSONArray();
//                            JSONObject pjobj = new JSONObject();
//                            JSONObject finalObj = new JSONObject();
//
//                            pjobj.put("costPrice", pricingBandMasterDetail.getPurchasePrice());
//                            pjobj.put("salesPrice", pricingBandMasterDetail.getSalesPrice());
//                            pjobj.put("productId", pricingBandMasterDetail.getProduct());
//                            pjobj.put("currencyId", (pricingBandMasterDetail.getCurrency() != null) ? pricingBandMasterDetail.getCurrency().getCurrencyID() : "");
//                            jArr.put(pjobj);
//                            finalObj.put("data", jArr);
//                            userData.put("data", finalObj);
//
//                            JSONObject resObj = APICallHandler.callApp(session, posURL, userData, companyID, action);
//                        }
                    }
                }
            }

            try {
                txnManager.commit(status);
                msg="Record Saved Successfully.";
            } catch (Exception ex) {
                isCommitEx = true;
            }
        } catch (Exception ex) {
            if (!isCommitEx) {
                txnManager.rollback(status);
            }
            issuccess = false;
            msg = "" + ex.getMessage();
            Logger.getLogger(accMasterItemsController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accMasterItemsController.class.getName()).log(Level.SEVERE, null, ex);
            }
//            HibernateUtil.closeSession(session);
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    
    public String getCurrencyIDFromIndex(HttpServletRequest request) throws SessionExpiredException, AccountingException, ServiceException {
        String currencyID = "";
        try {
            String column_Name = request.getParameter("column_Name") == null ? "" : request.getParameter("column_Name");
            
            if (column_Name.equalsIgnoreCase("purchasePriceUSD") || column_Name.equalsIgnoreCase("salesPriceUSD") || column_Name.equalsIgnoreCase("disocuntValueUSD") || column_Name.equalsIgnoreCase("discountmasterUSD")) {
                currencyID = "1";
            } else if (column_Name.equalsIgnoreCase("purchasePriceCAD") || column_Name.equalsIgnoreCase("salesPriceCAD") || column_Name.equalsIgnoreCase("disocuntValueCAD") || column_Name.equalsIgnoreCase("discountmasterCAD")) {
                currencyID = "10";
            } else if (column_Name.equalsIgnoreCase("purchasePriceAUD") || column_Name.equalsIgnoreCase("salesPriceAUD") || column_Name.equalsIgnoreCase("disocuntValueAUD") || column_Name.equalsIgnoreCase("discountmasterAUD")) {
                currencyID = "11";
            } else if (column_Name.equalsIgnoreCase("purchasePriceCNY") || column_Name.equalsIgnoreCase("salesPriceCNY") || column_Name.equalsIgnoreCase("disocuntValueCNY") || column_Name.equalsIgnoreCase("discountmasterCNY")) {
                currencyID = "12";
            } else if (column_Name.equalsIgnoreCase("purchasePriceIDR") || column_Name.equalsIgnoreCase("salesPriceIDR") || column_Name.equalsIgnoreCase("disocuntValueIDR") || column_Name.equalsIgnoreCase("discountmasterIDR")) {
                currencyID = "13";
            } else if (column_Name.equalsIgnoreCase("purchasePriceTWD") || column_Name.equalsIgnoreCase("salesPriceTWD") || column_Name.equalsIgnoreCase("disocuntValueTWD") || column_Name.equalsIgnoreCase("discountmasterTWD")) {
                currencyID = "14";
            } else if (column_Name.equalsIgnoreCase("purchasePriceTHB") || column_Name.equalsIgnoreCase("salesPriceTHB") || column_Name.equalsIgnoreCase("disocuntValueTHB") || column_Name.equalsIgnoreCase("discountmasterTHB")) {
                currencyID = "15";
            } else if (column_Name.equalsIgnoreCase("purchasePricePHP") || column_Name.equalsIgnoreCase("salesPricePHP") || column_Name.equalsIgnoreCase("disocuntValuePHP") || column_Name.equalsIgnoreCase("discountmasterPHP")) {
                currencyID = "16";
            } else if (column_Name.equalsIgnoreCase("purchasePriceNZD") || column_Name.equalsIgnoreCase("salesPriceNZD") || column_Name.equalsIgnoreCase("disocuntValueNZD") || column_Name.equalsIgnoreCase("discountmasterNZD")) {
                currencyID = "17";
            } else if (column_Name.equalsIgnoreCase("purchasePriceCHF") || column_Name.equalsIgnoreCase("salesPriceCHF") || column_Name.equalsIgnoreCase("disocuntValueCHF") || column_Name.equalsIgnoreCase("discountmasterCHF")) {
                currencyID = "18";
            } else if (column_Name.equalsIgnoreCase("purchasePriceGBP") || column_Name.equalsIgnoreCase("salesPriceGBP") || column_Name.equalsIgnoreCase("disocuntValueGBP") || column_Name.equalsIgnoreCase("discountmasterGBP")) {
                currencyID = "2";
            } else if (column_Name.equalsIgnoreCase("purchasePriceEUR") || column_Name.equalsIgnoreCase("salesPriceEUR") || column_Name.equalsIgnoreCase("disocuntValueEUR") || column_Name.equalsIgnoreCase("discountmasterEUR")) {
                currencyID = "3";
            } else if (column_Name.equalsIgnoreCase("purchasePriceINR") || column_Name.equalsIgnoreCase("salesPriceINR") || column_Name.equalsIgnoreCase("disocuntValueINR") || column_Name.equalsIgnoreCase("discountmasterINR")) {
                currencyID = "5";
            } else if (column_Name.equalsIgnoreCase("purchasePriceSGD") || column_Name.equalsIgnoreCase("salesPriceSGD") || column_Name.equalsIgnoreCase("disocuntValueSGD") || column_Name.equalsIgnoreCase("discountmasterSGD")) {
                currencyID = "6";
            } else if (column_Name.equalsIgnoreCase("purchasePriceMYR") || column_Name.equalsIgnoreCase("salesPriceMYR") || column_Name.equalsIgnoreCase("disocuntValueMYR") || column_Name.equalsIgnoreCase("discountmasterMYR")) {
                currencyID = "7";
            } else if (column_Name.equalsIgnoreCase("purchasePriceCRC") || column_Name.equalsIgnoreCase("salesPriceCRC") || column_Name.equalsIgnoreCase("disocuntValueCRC") || column_Name.equalsIgnoreCase("discountmasterCRC")) {
                currencyID = "8";
            } else if (column_Name.equalsIgnoreCase("purchasePriceUGX") || column_Name.equalsIgnoreCase("salesPriceUGX") || column_Name.equalsIgnoreCase("disocuntValueUGX") || column_Name.equalsIgnoreCase("discountmasterUGX")) {
                currencyID = "9";
            }else if (column_Name.equalsIgnoreCase("purchasePriceKRW") || column_Name.equalsIgnoreCase("salesPriceKRW") || column_Name.equalsIgnoreCase("disocuntValueKRW") || column_Name.equalsIgnoreCase("discountmasterKRW")) {
                currencyID = "19";
            }else if (column_Name.equalsIgnoreCase("purchasePriceAED") || column_Name.equalsIgnoreCase("salesPriceAED") || column_Name.equalsIgnoreCase("disocuntValueAED") || column_Name.equalsIgnoreCase("discountmasterAED")) {
                currencyID = "20";
            }else if (column_Name.equalsIgnoreCase("purchasePriceBND") || column_Name.equalsIgnoreCase("salesPriceBND") || column_Name.equalsIgnoreCase("disocuntValueBND") || column_Name.equalsIgnoreCase("discountmasterBND")) {
                currencyID = "21";
            }else if (column_Name.equalsIgnoreCase("purchasePriceHKD") || column_Name.equalsIgnoreCase("salesPriceHKD") || column_Name.equalsIgnoreCase("disocuntValueHKD") || column_Name.equalsIgnoreCase("discountmasterHKD")) {
                currencyID = "22";
            }else if (column_Name.equalsIgnoreCase("purchasePriceJPY") || column_Name.equalsIgnoreCase("salesPriceJPY") || column_Name.equalsIgnoreCase("disocuntValueJPY") || column_Name.equalsIgnoreCase("discountmasterJPY")) {
                currencyID = "23";
            }else if (column_Name.equalsIgnoreCase("purchasePriceVND") || column_Name.equalsIgnoreCase("salesPriceVND") || column_Name.equalsIgnoreCase("disocuntValueVND") || column_Name.equalsIgnoreCase("discountmasterVND")) {
                currencyID = "24";
            }else if (column_Name.equalsIgnoreCase("purchasePriceOMR") || column_Name.equalsIgnoreCase("salesPriceOMR") || column_Name.equalsIgnoreCase("disocuntValueOMR") || column_Name.equalsIgnoreCase("discountmasterOMR")) {
                currencyID = "25";
            }else if (column_Name.equalsIgnoreCase("purchasePriceCNH") || column_Name.equalsIgnoreCase("salesPriceCNH") || column_Name.equalsIgnoreCase("disocuntValueCNH") || column_Name.equalsIgnoreCase("discountmasterCNH")) {
                currencyID = "26";
            }else if (column_Name.equalsIgnoreCase("purchasePriceSAR") || column_Name.equalsIgnoreCase("salesPriceSAR") || column_Name.equalsIgnoreCase("discountmasterSAR") || column_Name.equalsIgnoreCase("discountmasterCNH")) {
                currencyID = "27";                                                                                                                                                                                                                                                      
            }else if (column_Name.equalsIgnoreCase("purchasePriceZAR") || column_Name.equalsIgnoreCase("salesPriceZAR") || column_Name.equalsIgnoreCase("disocuntValueZAR") || column_Name.equalsIgnoreCase("discountmasterCNH")) {
                currencyID = "28";
            }else if (column_Name.equalsIgnoreCase("purchasePriceSEK") || column_Name.equalsIgnoreCase("salesPriceSEK") || column_Name.equalsIgnoreCase("disocuntValueSEK") || column_Name.equalsIgnoreCase("discountmasterCNH")) {
                currencyID = "29";
            }else if (column_Name.equalsIgnoreCase("purchasePriceBDT") || column_Name.equalsIgnoreCase("salesPriceBDT") || column_Name.equalsIgnoreCase("disocuntValueBDT") || column_Name.equalsIgnoreCase("discountmasterCNH")) {
                currencyID = "30";
            }else if (column_Name.equalsIgnoreCase("purchasePriceMMK") || column_Name.equalsIgnoreCase("salesPriceMMK") || column_Name.equalsIgnoreCase("disocuntValueMMK") || column_Name.equalsIgnoreCase("discountmasterCNH")) {
                currencyID = "31";
            }else if (column_Name.equalsIgnoreCase("purchasePriceNGN") || column_Name.equalsIgnoreCase("salesPriceNGN") || column_Name.equalsIgnoreCase("disocuntValueNGN") || column_Name.equalsIgnoreCase("discountmasterCNH")) {
                currencyID = "32";
            }

        } catch (Exception ex) {
            throw new AccountingException(messageSource.getMessage("acc.common.excp1", null, RequestContextUtils.getLocale(request)));
        }
        return currencyID;
    }
    
    public ModelAndView deletePricingBands(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("SO_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        try {
            String linkedTransaction = deletePricingBands(request);

            txnManager.commit(status);
            issuccess = true;
            if (StringUtil.isNullOrEmpty(linkedTransaction)) {
                msg = messageSource.getMessage("acc.pricingBand.del", null, RequestContextUtils.getLocale(request));   // "Pricing Band has been deleted successfully";
            } else {
                msg = messageSource.getMessage("acc.field.pricingBandsExcept", null, RequestContextUtils.getLocale(request)) + " " + linkedTransaction.substring(0, linkedTransaction.length() - 2) + " " + messageSource.getMessage("acc.field.hasbeendeletedsuccessfully", null, RequestContextUtils.getLocale(request));   // "Pricing Bands Except " + linkedTransaction.substring(0, linkedTransaction.length() - 2) + " has been deleted successfully.";
            }
        } catch (SessionExpiredException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            Logger.getLogger(accMasterItemsController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            txnManager.rollback(status);
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
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    
    public String deletePricingBands(HttpServletRequest request) throws SessionExpiredException, AccountingException, ServiceException {
        String linkedTransaction = "";
        try {
            JSONArray jArr = new JSONArray(request.getParameter("data"));
            String companyID = sessionHandlerImpl.getCompanyid(request);

            String pricingBandID = "", pricingBandName = "";
            for (int i = 0; i < jArr.length(); i++) {
                JSONObject jobj = jArr.getJSONObject(i);
                pricingBandID = StringUtil.DecodeText(jobj.optString("id"));
                pricingBandName = StringUtil.DecodeText(jobj.optString("name"));
                
                HashMap<String, Object> requestParams = new HashMap<String, Object>();
                requestParams.put("pricingBandID", pricingBandID);
                requestParams.put("companyID", companyID);
                requestParams.put("pricingBandName", pricingBandName);

                if (!StringUtil.isNullOrEmpty(pricingBandName)) {
                    KwlReturnObject result = accMasterItemsDAOobj.getPricingBandFromCustomer(pricingBandID, companyID);
                    List list = result.getEntityList();
                    if (!list.isEmpty()) {
                        linkedTransaction += pricingBandName + ", ";
                        continue;
                    }
                    KwlReturnObject result1 = accMasterItemsDAOobj.getPricingBandFromVendor(pricingBandID, companyID);
                    List list1 = result1.getEntityList();
                    if (!list1.isEmpty()) {
                        linkedTransaction += pricingBandName + ", ";
                        continue;
                    }
                }
                accMasterItemsDAOobj.deleteProductBrandDiscountDetails(requestParams);
                accMasterItemsDAOobj.deletePricingBandDetails(requestParams);
                accMasterItemsDAOobj.deletePricingBand(requestParams);
                auditTrailObj.insertAuditLog(AuditAction.PRICING_BAND_CHANGED, "User " + sessionHandlerImpl.getUserFullName(request) + " has deleted a Pricing Band " + pricingBandName, request, pricingBandID);
            }
        } /*catch (UnsupportedEncodingException ex) {
            throw ServiceException.FAILURE(messageSource.getMessage("acc.common.excp", null, RequestContextUtils.getLocale(request)), ex);
        }*/ catch (JSONException ex) {
            throw new AccountingException(messageSource.getMessage("acc.common.excp1", null, RequestContextUtils.getLocale(request)));
        }
        return linkedTransaction;
    }
    
    public ModelAndView sendProductsPriceToPOS(HttpServletRequest request, HttpServletResponse response) throws ServiceException {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        //Session session=null;
        try {
            //session = HibernateUtil.getCurrentSession();
            String companyID = sessionHandlerImpl.getCompanyid(request);
            String companySubDomain = sessionHandlerImpl.getCompanySessionObj(request).getCdomain();
            String posURL = this.getServletContext().getInitParameter("posURL");
            String action = "33";

            JSONObject userData = new JSONObject();
            userData.put("iscommit", true);
            userData.put("remoteapikey", StorageHandler.GetRemoteAPIKey());
            userData.put("userid", sessionHandlerImpl.getUserid(request));
            userData.put("companyid", companyID);
            userData.put("subdomain", companySubDomain);
            userData.put("action", action);

            JSONObject pjobj = getPOSProductsPrice(request, response);
            userData.put("data", pjobj);

            JSONObject resObj = apiCallHandlerService.callApp(posURL, userData, companyID, action);

            if (!resObj.isNull("success") && resObj.getBoolean("success")) {
                issuccess = resObj.getBoolean("success");
                msg = resObj.getString("msg");
                jobj.put("success", true);
                jobj.put("msg", msg);
//                jobj.put("companyexist", resObj.optBoolean("companyexist"));
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("crmManager.insertAccProduct", ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accMasterItemsController.class.getName()).log(Level.SEVERE, null, ex);
            }
//            HibernateUtil.closeSession(session);;No need to close seesion hibernate manage it automatically
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public JSONObject getPOSProductsPrice(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        JSONArray DataJArr = new JSONArray();
        boolean issuccess = false;
        String msg = "";
        try {
            boolean isSyncToPOS = StringUtil.isNullOrEmpty(request.getParameter("isSyncToPOS")) ? false : Boolean.parseBoolean(request.getParameter("isSyncToPOS"));
            
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            requestParams.put("isSyncToPOS", isSyncToPOS);
            requestParams.put("companyID", sessionHandlerImpl.getCompanyid(request));
            String productIds=request.getParameter("productIds");
            if(!StringUtil.isNullOrEmpty(productIds)){
            requestParams.put("productIds", productIds);    
            }
            if(isSyncToPOS){
                Date date =new Date();
                requestParams.put("applicableDate", date);
            }
            KwlReturnObject result = accMasterItemsDAOobj.getPOSProductsPrice(requestParams);

            List<PricingBandMasterDetail> list = result.getEntityList();
            DataJArr = getPOSProductsPriceJson(request, list);
            int totalCount = result.getRecordTotalCount();

            jobj.put("data", DataJArr);
            jobj.put("totalCount", totalCount);
            issuccess = true;
        } catch (SessionExpiredException ex) {
            msg = ex.getMessage();
            Logger.getLogger(accMasterItemsController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ServiceException ex) {
            msg = ex.getMessage();
            Logger.getLogger(accMasterItemsController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
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
    
    public JSONArray getPOSProductsPriceJson(HttpServletRequest request, List<PricingBandMasterDetail> list) throws JSONException, ServiceException {
        JSONArray jArr = new JSONArray();
        try {
            for (PricingBandMasterDetail pricingBandMasterDetailObj : list) {
                JSONObject obj = new JSONObject();
                obj.put("productId", pricingBandMasterDetailObj.getProduct() != null ? pricingBandMasterDetailObj.getProduct() : "");
                obj.put("costPrice", pricingBandMasterDetailObj.getPurchasePrice());
                obj.put("salesPrice", pricingBandMasterDetailObj.getSalesPrice());
                obj.put("currencyId", pricingBandMasterDetailObj.getCurrency() != null ? pricingBandMasterDetailObj.getCurrency().getCurrencyID() : "");
                jArr.put(obj);
            }
        } catch (Exception ex) {
            Logger.getLogger(accMasterItemsController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return jArr;
    }

    public ModelAndView getLocationLevels(HttpServletRequest request,HttpServletResponse response)throws ServiceException{
          JSONArray dataJArr = new JSONArray();
          JSONObject jobj=new JSONObject();
        boolean issuccess = false;
        String msg = "";
        try {
            String companyID = sessionHandlerImpl.getCompanyid(request);
              KwlReturnObject result = accMasterItemsDAOobj.getLocationLevelMapping(companyID);
                    List<Object> resultList = result.getEntityList();
                    if (!resultList.isEmpty()) {
                        for(Object resultObj:resultList){
                            JSONObject jSONObject=new JSONObject();
                            LocationLevelMapping locationLevelMapping=(LocationLevelMapping)resultObj;
                            jSONObject.put("levelMapId", locationLevelMapping.getID());
                            jSONObject.put("levelName", locationLevelMapping.getNewLevelNm());
                            dataJArr.put(jSONObject);
                        }
                    }else{
                        result = accMasterItemsDAOobj.getLocationLevel();
                        resultList = result.getEntityList();
                        if (!resultList.isEmpty()) {
                            for(Object resultObj:resultList){
                                JSONObject jSONObject=new JSONObject();
                                LocationLevel locationLevel=(LocationLevel)resultObj;
                                jSONObject.put("levelId", locationLevel.getId());
                                jSONObject.put("levelName", locationLevel.getName());
                                dataJArr.put(jSONObject);
                        }
                    }
                }
            jobj.put("data", dataJArr);        
        }catch (Exception ex) {
            throw ServiceException.FAILURE("crmManager.insertAccProduct", ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accMasterItemsController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    
       public ModelAndView getLevelNames(HttpServletRequest request,HttpServletResponse response)throws ServiceException{
          JSONArray dataJArr = new JSONArray();
          JSONObject jobj=new JSONObject();
        boolean issuccess = false;
         
         try{
         String companyID = sessionHandlerImpl.getCompanyid(request);
         KwlReturnObject presult=accMasterItemsDAOobj.getCompanPreferencesSql(companyID);
         
         if (presult.getEntityList() != null && !presult.getEntityList().isEmpty()) {
                 Object[] prefObj = (Object[]) presult.getEntityList().get(0);
         
        String msg = "";
        try {
            KwlReturnObject result = accMasterItemsDAOobj.getLocationLevel();
            List<Object> resultList = result.getEntityList();
            if (!resultList.isEmpty()) {
                 JSONObject jSONObject=new JSONObject();
                 jSONObject.put("levelId", "0");
                 jSONObject.put("levelName", "_");
                 dataJArr.put(jSONObject);
                 int cntr=0;
                for(Object resultObj:resultList){
                    jSONObject=new JSONObject();
                    LocationLevel locationLevel=(LocationLevel)resultObj;
                    if(locationLevel.getId()<6)//department
                    {
                        boolean perm=(Boolean)(prefObj[cntr]);
                        if(perm){
                            jSONObject.put("levelId", locationLevel.getId());
                            jSONObject.put("levelName", locationLevel.getName());
                            dataJArr.put(jSONObject);
                       }
                    }
                    cntr++;
                }
            }
            issuccess=true;
            jobj.put("data", dataJArr);        
        }catch (Exception ex) {
            throw ServiceException.FAILURE("crmManager.insertAccProduct", ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accMasterItemsController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
         }
         }catch(SessionExpiredException e){
              throw ServiceException.FAILURE("crmManager.insertAccProduct", e);
         }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
     public ModelAndView getLevels(HttpServletRequest request,HttpServletResponse response)throws ServiceException{
          JSONArray dataJArr = new JSONArray();
          JSONObject jobj=new JSONObject();
        boolean issuccess = false;
        String msg = "";
        String parent="";
        try {
            String companyID = sessionHandlerImpl.getCompanyid(request);
            Object[] prefObj=null;
           KwlReturnObject presult=accMasterItemsDAOobj.getCompanPreferencesSql(companyID);
            if (presult.getEntityList() != null && !presult.getEntityList().isEmpty()) {
                    prefObj = (Object[]) presult.getEntityList().get(0);
            }
            KwlReturnObject result = accMasterItemsDAOobj.getLocationLevelMapping(companyID);
            List<Object> resultList = result.getEntityList();
            if (!resultList.isEmpty()) {
                for(Object resultObj:resultList){
                    JSONObject jSONObject=new JSONObject();
                    LocationLevelMapping locationLevelmap=(LocationLevelMapping)resultObj;
                    
                    LocationLevel llevel=locationLevelmap.getLlevelid();
                    if(prefObj.length>0 && (Boolean)prefObj[5]){
                        if(llevel.getId()==1) parent="0";
                        else if(llevel.getId()==2) parent="1";
                        else parent=locationLevelmap.getParent();
                    }else{
                        parent=locationLevelmap.getParent();
                    }
                    
                    jSONObject.put("Id", locationLevelmap.getID());
                    jSONObject.put("levelName",llevel.getName());
                    jSONObject.put("newLevelName", locationLevelmap.getNewLevelNm().equals("") ? llevel.getName() : locationLevelmap.getNewLevelNm());
                    jSONObject.put("parent", parent);
                    jSONObject.put("isActivate", locationLevelmap.isActivate());
                    jSONObject.put("levelId", llevel.getId());
                    dataJArr.put(jSONObject);
                }
            }else{
                 result = accMasterItemsDAOobj.getLocationLevel();
                 resultList = result.getEntityList();
                 if (!resultList.isEmpty()) {
                    for(Object resultObj:resultList){
                        JSONObject jSONObject=new JSONObject();
                        LocationLevel locationLevel=(LocationLevel)resultObj;
                        if(prefObj.length>0 && (Boolean)prefObj[5]){
                            if(locationLevel.getId()==1) parent="0";
                            else if(locationLevel.getId()==2) parent="1";
                            else parent="";
                        }else{
                            parent="";
                        }

                        jSONObject.put("Id", locationLevel.getId());
                        jSONObject.put("levelName",locationLevel.getName());
                        jSONObject.put("newLevelName",locationLevel.getName()); 
                        jSONObject.put("parent", parent);
                        jSONObject.put("isActivate", false);
                        jSONObject.put("levelId", locationLevel.getId());
                        dataJArr.put(jSONObject);
                    }
            }
            }
            jobj.put("data", dataJArr);        
        }catch (Exception ex) {
            throw ServiceException.FAILURE("crmManager.insertAccProduct", ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accMasterItemsController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    public ModelAndView getLevelsCombo(HttpServletRequest request,HttpServletResponse response)throws ServiceException{
          JSONArray dataJArr = new JSONArray();
          JSONObject jobj=new JSONObject();
        boolean issuccess = false;
        String msg = "";
        try {
            String companyID = sessionHandlerImpl.getCompanyid(request);
            KwlReturnObject result = accMasterItemsDAOobj.getLocationLevelMapping(companyID);
            List<Object> resultList = result.getEntityList();
            if (!resultList.isEmpty()) {
                for(Object resultObj:resultList){
                    JSONObject jSONObject=new JSONObject();
                    LocationLevelMapping locationLevelmap=(LocationLevelMapping)resultObj;
                    LocationLevel llevel=locationLevelmap.getLlevelid();
                    if(llevel.getId()!=6){
                        jSONObject.put("Id", locationLevelmap.getID());
                        jSONObject.put("levelName", locationLevelmap.getNewLevelNm());
                        jSONObject.put("parent", locationLevelmap.getParent());
                        jSONObject.put("levelId", llevel.getId());
                        dataJArr.put(jSONObject);
                    }
                }
            }else{
                 result = accMasterItemsDAOobj.getLocationLevel();
                 resultList = result.getEntityList();
                 if (!resultList.isEmpty()) {
                    for(Object resultObj:resultList){
                        JSONObject jSONObject=new JSONObject();
                        LocationLevel locationLevel=(LocationLevel)resultObj;
                       if(locationLevel.getId()!=6){
                        jSONObject.put("Id", locationLevel.getId());
                        jSONObject.put("levelName",locationLevel.getName());
                        jSONObject.put("parent", "");
                        jSONObject.put("levelId", locationLevel.getId());
                        dataJArr.put(jSONObject);
                       }
                    }
            }
            }
            jobj.put("data", dataJArr);        
        }catch (Exception ex) {
            throw ServiceException.FAILURE("crmManager.insertAccProduct", ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accMasterItemsController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    public ModelAndView savePriceListVolumeDiscount(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false, isCommitEx = false;
        String msg = "";
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("Account_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        try {
            KwlReturnObject result = savePriceListVolumeDiscountItem(request);
            issuccess = true;
            msg = result.getMsg();
            if (result.getEntityList() != null) {
                PricingBandMaster pricingBandMaster = (PricingBandMaster) result.getEntityList().get(0);
                jobj.put("id", pricingBandMaster.getID());
                String name = request.getParameter("name");
                boolean isEdit = StringUtil.isNullOrEmpty(request.getParameter("isEdit")) ? false : Boolean.parseBoolean(request.getParameter("isEdit"));
                String action = "added";
                if (isEdit) {
                    action = "updated";
                }
                auditTrailObj.insertAuditLog(AuditAction.PRICE_LIST_VOLUME_DISCOUNT_CHANGED, "User " + sessionHandlerImpl.getUserFullName(request) + " has " + action + " Price List - Volume Discount item " + name, request, "0");

            } else {
                issuccess = false;
            }

            try {
             txnManager.commit(status);
            } catch (Exception ex) {
                isCommitEx = true;
            }

        } catch (Exception ex) {
            if (!isCommitEx) {
                txnManager.rollback(status);
            }
            issuccess = false;
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
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public KwlReturnObject savePriceListVolumeDiscountItem(HttpServletRequest request) throws SessionExpiredException, ServiceException {
        KwlReturnObject result = null;
        String msg = messageSource.getMessage("acc.priceListVolumeDiscount.saveMsg", null, RequestContextUtils.getLocale(request));   // "Price List - Volume Discount item has been saved successfully.";
        boolean isPresent = false;
        String itemID = request.getParameter("id");
        int pricePolicyValue = StringUtil.isNullOrEmpty(request.getParameter("pricePolicyValue")) ? 1 : Integer.parseInt(request.getParameter("pricePolicyValue")); // 1 - Use Discount, 2 - Use Flat Price

        HashMap requestParam = AccountingManager.getGlobalParams(request);
        requestParam.put("id", itemID);
        requestParam.put("name", request.getParameter("name"));
        requestParam.put("pricePolicyValue", pricePolicyValue);
        requestParam.put("desc", request.getParameter("desc"));
        requestParam.put("volumeDiscount", true);

        HashMap<String, Object> filterRequestParams = new HashMap<String, Object>();
        filterRequestParams.put("name", request.getParameter("name"));
        filterRequestParams.put("companyid", sessionHandlerImpl.getCompanyid(request));

        KwlReturnObject cntResult = accMasterItemsDAOobj.getPriceListVolumeDiscountItems(filterRequestParams);
        int count = cntResult.getRecordTotalCount();

        if (count == 1) {
            String recordID = ((PricingBandMaster) cntResult.getEntityList().get(0)).getID();
            isPresent = itemID.equals(recordID) ? false : true; // Allow Editing same record
        } else if (count > 1) {
            isPresent = true;
        }

        if (isPresent) {
            msg = "Price List - Volume Discount item entry for <b>" + request.getParameter("name") + "</b> already exists.";
            return new KwlReturnObject(false, msg, null, null, 0);
        } else {
            result = accMasterItemsDAOobj.addPriceListVolumeDiscountItem(requestParam);
        }
        return new KwlReturnObject(true, msg, null, result.getEntityList(), 0);
    }
    
    public ModelAndView getPriceListVolumeDiscount(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        try {
            jobj = getPriceListVolumeDiscountItems(request);
            issuccess = true;
        } catch (SessionExpiredException ex) {
            msg = ex.getMessage();
            Logger.getLogger(accMasterItemsController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ServiceException ex) {
            msg = ex.getMessage();
            Logger.getLogger(accMasterItemsController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            msg = "accMasterItemsController.getLocationItems : " + ex.getMessage();
            Logger.getLogger(accMasterItemsController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accMasterItemsController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public JSONObject getPriceListVolumeDiscountItems(HttpServletRequest request) throws ServiceException, SessionExpiredException {
        JSONObject jobj = new JSONObject();
        try {
            HashMap<String, Object> filterRequestParams = new HashMap<String, Object>();
            filterRequestParams.put("companyid", sessionHandlerImpl.getCompanyid(request));
            KwlReturnObject result = accMasterItemsDAOobj.getPriceListVolumeDiscountItems(filterRequestParams);

            List<PricingBandMaster> list = result.getEntityList();
            JSONArray jArr = new JSONArray();
            
            for (PricingBandMaster pricingBandMaster : list) {
                JSONObject obj = new JSONObject();
                obj.put("id", pricingBandMaster.getID());
                obj.put("name", pricingBandMaster.getName());
                obj.put("pricePolicyValue", pricingBandMaster.getPricePolicyValue());
                obj.put("desc", pricingBandMaster.getDescription());
                jArr.put(obj);
            }
            jobj.put("data", jArr);
        } catch (JSONException e) {
            throw ServiceException.FAILURE(e.getMessage(), e);
        }
        return jobj;
    }
    
    public ModelAndView deletePriceListVolumeDiscount(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("SO_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        try {
            String linkedTransaction = deletePriceListVolumeDiscount(request);

            txnManager.commit(status);
            issuccess = true;
            if (StringUtil.isNullOrEmpty(linkedTransaction)) {
                msg = messageSource.getMessage("acc.priceListVolumeDiscount.delMsg", null, RequestContextUtils.getLocale(request));   // "Price List - Volume Discount has been deleted successfully.";
            } else {
                msg = messageSource.getMessage("acc.field.priceListVolumeDiscountsExcept", null, RequestContextUtils.getLocale(request)) + " " + linkedTransaction.substring(0, linkedTransaction.length() - 2) + " " + messageSource.getMessage("acc.field.hasbeendeletedsuccessfully", null, RequestContextUtils.getLocale(request));   // "Price List - Volume Discounts Except " + linkedTransaction.substring(0, linkedTransaction.length() - 2) + " has been deleted successfully.";
            }
        } catch (SessionExpiredException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            Logger.getLogger(accMasterItemsController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            txnManager.rollback(status);
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
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public String deletePriceListVolumeDiscount(HttpServletRequest request) throws SessionExpiredException, AccountingException, ServiceException {
        String linkedTransaction = "";
        try {
            JSONArray jArr = new JSONArray(request.getParameter("data"));
            String companyID = sessionHandlerImpl.getCompanyid(request);

            String priceListVolumeDiscountID = "", priceListVolumeDiscountName = "";
            for (int i = 0; i < jArr.length(); i++) {
                JSONObject jobj = jArr.getJSONObject(i);
                priceListVolumeDiscountID = StringUtil.DecodeText(jobj.optString("id"));
                priceListVolumeDiscountName = jobj.getString("name");

                HashMap<String, Object> requestParams = new HashMap<String, Object>();
                requestParams.put("priceListVolumeDiscountID", priceListVolumeDiscountID);
                requestParams.put("companyID", companyID);
                requestParams.put("pricingBandName", priceListVolumeDiscountName);

//                if (!StringUtil.isNullOrEmpty(priceListVolumeDiscountName)) {
//                    KwlReturnObject result = accMasterItemsDAOobj.getPricingBandFromCustomer(priceListVolumeDiscountID, companyID);
//                    List list = result.getEntityList();
//                    if (!list.isEmpty()) {
//                        linkedTransaction += priceListVolumeDiscountName + ", ";
//                        continue;
//                    }
//                    KwlReturnObject result1 = accMasterItemsDAOobj.getPricingBandFromVendor(priceListVolumeDiscountID, companyID);
//                    List list1 = result1.getEntityList();
//                    if (!list1.isEmpty()) {
//                        linkedTransaction += priceListVolumeDiscountName + ", ";
//                        continue;
//                    }
//                }
                
                if (!StringUtil.isNullOrEmpty(priceListVolumeDiscountName)) {
                    String result = accMasterItemsDAOobj.getPricingbandMappedwithvolumeDisc(priceListVolumeDiscountID);
                    if (!"".equals(result)) {
                        linkedTransaction += priceListVolumeDiscountName + ", ";
                        continue;
                    } 
                }
                accMasterItemsDAOobj.deletePriceListVolumeDiscountDetails(requestParams);
                accMasterItemsDAOobj.deletePriceListVolumeDiscount(requestParams);
                auditTrailObj.insertAuditLog(AuditAction.PRICE_LIST_VOLUME_DISCOUNT_CHANGED, "User " + sessionHandlerImpl.getUserFullName(request) + " has deleted a Price List - Volume Discount " + priceListVolumeDiscountName, request, priceListVolumeDiscountID);
            }
        } /*catch (UnsupportedEncodingException ex) {
            throw ServiceException.FAILURE(messageSource.getMessage("acc.common.excp", null, RequestContextUtils.getLocale(request)), ex);
        } */catch (JSONException ex) {
            throw new AccountingException(messageSource.getMessage("acc.common.excp1", null, RequestContextUtils.getLocale(request)));
        }
        return linkedTransaction;
    }

     public ModelAndView updateMasterSetting(HttpServletRequest request,HttpServletResponse response) throws SessionExpiredException, ServiceException {
          JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        TransactionStatus status=null;
        try{
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("Account_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
       
        
        String levelIds[] = request.getParameterValues("levelId");
        String levelnm[] = request.getParameterValues("levelnm");
        String newLevelNm[] = request.getParameterValues("newLevelNm");
        String isActivate[] = request.getParameterValues("isActivate");
        String parent[] = request.getParameterValues("parent");
        String CompanyId=sessionHandlerImpl.getCompanyid(request);
        
        String errorMsg=validateMasterSetting(levelIds,parent);
        if(errorMsg.equals("")){
        KwlReturnObject result = accMasterItemsDAOobj.getLocationLevelMapping(CompanyId);
        List<Object> resultList = result.getEntityList();
        String id="";
       
        for(int i=0;i<levelIds.length;i++){
             status = txnManager.getTransaction(def);
             HashMap<String, Object> requestParam = new HashMap<String, Object>();
            if (!resultList.isEmpty()) {
                LocationLevelMapping levelMapping = (LocationLevelMapping) resultList.get(i);
                id = levelMapping.getID();
                requestParam.put("id", id);
            }
            
            
            requestParam.put("newLevelName", newLevelNm[i].equals("") ? levelnm[i] : newLevelNm[i]);
            requestParam.put("parent",parent[i].equals("")? "0": parent[i]);
            requestParam.put("activate", isActivate[i]);
            requestParam.put("levelId", levelIds[i]);
            requestParam.put("company", CompanyId);
            
            result = accMasterItemsDAOobj.updateMasterSetting(requestParam);
            txnManager.commit(status);
            issuccess=true;
        }
       }else{
           issuccess=false;
           msg=errorMsg;
        }
        }catch (SessionExpiredException ex) {
            if(status != null) {
                txnManager.rollback(status);
            }
            msg = ex.getMessage();
            Logger.getLogger(accMasterItemsController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            if(status != null) {
                txnManager.rollback(status);
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
       return new ModelAndView("jsonView", "model", jobj.toString());
    }
    public ModelAndView resetLocationLevelMapping(HttpServletRequest request,HttpServletResponse response) throws SessionExpiredException, ServiceException {
          JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        TransactionStatus status=null;
        try{
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("Account_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
       
        
        boolean isInventoryTabActive=Boolean.parseBoolean(request.getParameter("isInventoryTabActive"));
        String CompanyId=sessionHandlerImpl.getCompanyid(request);
        
        KwlReturnObject result = accMasterItemsDAOobj.getLocationLevelMapping(CompanyId);
        List<Object> resultList = result.getEntityList();
        String id="";
       
        for(Object resultObj : resultList){
             status = txnManager.getTransaction(def);
             HashMap<String, Object> requestParam = new HashMap<String, Object>();
             LocationLevelMapping levelMapping = (LocationLevelMapping) resultObj;
             requestParam.put("id",levelMapping.getID());
             String parent="0";
             if(isInventoryTabActive){
                 if(levelMapping.getLlevelid().getId()==2)
                     parent="1";
             }
            requestParam.put("parent",parent);
            result = accMasterItemsDAOobj.updateMasterSetting(requestParam);
            txnManager.commit(status);
            issuccess=true;
        }
       
        }catch (SessionExpiredException ex) {
            if(status != null) {
                txnManager.rollback(status);
            }
            msg = ex.getMessage();
            Logger.getLogger(accMasterItemsController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            if(status != null) {
                txnManager.rollback(status);
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
       return new ModelAndView("jsonView", "model", jobj.toString());
    } 
     public String validateMasterSetting(String levelIds[],String parent[]){
         String errorMsg="";
         boolean errorFlag=false;
         try{
             for(int i=0;i<levelIds.length;i++){
                 if(parent[i].equals("")) parent[i]="0";
                 if(Integer.parseInt(levelIds[i])==Integer.parseInt(parent[i])){
                     errorMsg="Parent and Child cannot be Same at row No. "+(i+1);
                     errorFlag=true;
                     break;
                 }
             }   
             for(int i=0;i<levelIds.length && !errorFlag;i++){
                 int parentValue=Integer.parseInt(parent[i]);
                 int l=Integer.parseInt(levelIds[i]);
                 while (parentValue > 0) {
                     String temp=parent[parentValue - 1];
                     if(temp==null) temp="0";
                     if(temp.equals("")) temp="0";
                     if (l == Integer.parseInt(temp)) {
                         errorMsg = "Parent-child recursion not allowed";
                         errorFlag=true;
                         break;
                     } else {
                         parentValue = Integer.parseInt(parent[parentValue - 1]);
                     }
                 }
                 int p=Integer.parseInt(parent[i]);
                 for(int j=0;j<i;j++){
                     if(p>0 && p==Integer.parseInt(parent[j])){
                         errorMsg="Same parents are not allowed";
                         errorFlag=true;
                         break;
                     }
                 }
                 if(errorFlag) break;
             }
         }catch(Exception e){
             errorMsg=e.toString();
         }
         
         return errorMsg;
     }
     /**
      * @Desc : Get Packing Store for Pick Pack Ship
      * @param request
      * @param response
      * @return 
      */
       public ModelAndView getPackingStore(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        try {
            jobj = getPackingStore(request);
            issuccess = true;
        } catch (SessionExpiredException ex) {
            msg = ex.getMessage();
            Logger.getLogger(accMasterItemsController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } catch (ServiceException ex) {
            msg = ex.getMessage();
            Logger.getLogger(accMasterItemsController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } catch (Exception ex) {
            msg = "accMasterItemsController.getStoreMasters : " + ex.getMessage();
            Logger.getLogger(accMasterItemsController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accMasterItemsController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
       
    public JSONObject getPackingStore(HttpServletRequest request) throws ServiceException, SessionExpiredException {
        JSONObject jobj = new JSONObject();
        try {
            String companyid = sessionHandlerImpl.getCompanyid(request);
            Store store=null;
            Location location=null;
            String packingstorelocid="";
             KwlReturnObject StorelocObjresult=null;
            KwlReturnObject CompanyResult = accountingHandlerDAOobj.getObject(Company.class.getName(), (String) companyid);
            Company company = (Company) CompanyResult.getEntityList().get(0);
            
            KwlReturnObject extracompanyprefObjresult = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), company.getCompanyID());
            ExtraCompanyPreferences extracompanyobj = (ExtraCompanyPreferences) extracompanyprefObjresult.getEntityList().get(0);
            
            if (extracompanyobj.isInterloconpick()) {
                packingstorelocid = extracompanyobj.getPackinglocation();
            } else {
                packingstorelocid = extracompanyobj.getPackingstore();
            }
            
            if (extracompanyobj.isInterloconpick() && !StringUtil.isNullOrEmpty(packingstorelocid)) {
                packingstorelocid = extracompanyobj.getPackinglocation();
                StorelocObjresult = accountingHandlerDAOobj.getObject(Location.class.getName(), packingstorelocid);
            } else {
                if (!StringUtil.isNullOrEmpty(packingstorelocid)) {
                    packingstorelocid = extracompanyobj.getPackingstore();
                    StorelocObjresult = accountingHandlerDAOobj.getObject(Store.class.getName(), packingstorelocid);
                }
            }

            JSONObject obj = new JSONObject();
            JSONArray jArr = new JSONArray();
            if (StorelocObjresult != null) {
                if (extracompanyobj.isInterloconpick()) {
                    location =(Location) StorelocObjresult.getEntityList().get(0);
                    obj.put("packlocation", location != null ? location.getId() : "");
                    obj.put("fullname", location != null ? location.getName() : "");
                } else {
                    store = (Store) StorelocObjresult.getEntityList().get(0);
                    obj.put("packwarehouse", store != null ? store.getId() : "");
                    obj.put("fullname", store != null ? store.getFullName() : "");
                    if(!StringUtil.isNullOrEmpty(store.getDefaultLocation().getId())){
//                        StorelocObjresult = accountingHandlerDAOobj.getObject(Location.class.getName(),store.getDefaultLocation()+"");     
//                        location =(Location) StorelocObjresult.getEntityList().get(0);
                        obj.put("packinglocationid", store != null ? store.getDefaultLocation().getId(): "");
                        obj.put("packinglocationname", store != null ? store.getDefaultLocation().getName(): "");
                    }
                    
                }
            }
            jArr.put(obj);
            jobj.put("data", jArr);
        } catch (JSONException e) {
            throw ServiceException.FAILURE(e.getMessage(), e);
        }
        return jobj;
    }
  public ModelAndView getStoreMasters(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        try {
            jobj = getStoreMasters(request);
            issuccess = true;
        } catch (SessionExpiredException ex) {
            msg = ex.getMessage();
            Logger.getLogger(accMasterItemsController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } catch (ServiceException ex) {
            msg = ex.getMessage();
            Logger.getLogger(accMasterItemsController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } catch (Exception ex) {
            msg = "accMasterItemsController.getStoreMasters : " + ex.getMessage();
            Logger.getLogger(accMasterItemsController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accMasterItemsController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }    
     public JSONObject getStoreMasters(HttpServletRequest request) throws ServiceException, SessionExpiredException {
        JSONObject jobj = new JSONObject();
        try {
            String gNm=request.getParameter("transType");
            String ss = request.getParameter("ss");
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
            filter_params.add(sessionHandlerImpl.getCompanyid(request));
            filter_params.add(type);
            if(!StringUtil.isNullOrEmpty(ss)){
                filterRequestParams.put("ss",ss);
                filterRequestParams.put("ss_names",  new String[]{"name"});
            }
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
    public ModelAndView saveStoreMasterItem(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
//        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
//        def.setName("MI_Tx");
//        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
//        TransactionStatus status = txnManager.getTransaction(def);
        try {
            KwlReturnObject result = saveStoreMasterItem(request);
            issuccess = true;
            msg = result.getMsg();
            if (result.getEntityList() != null) {
                StoreMaster storeMaster = (StoreMaster) result.getEntityList().get(0);
                jobj.put("id", storeMaster.getId());
                String name = request.getParameter("name");
                String groupName = request.getParameter("groupName");
                String oldName =(!StringUtil.isNullOrEmpty(request.getParameter("oldName")))? request.getParameter("oldName") : "";
              
                boolean isEdit = StringUtil.isNullOrEmpty(request.getParameter("isEdit")) ? false : Boolean.parseBoolean(request.getParameter("isEdit"));
                String action = "added";
                if (isEdit) {
                    action = "updated";
                    auditTrailObj.insertAuditLog(AuditAction.WAREHOUSE_CHANGED, "User " + sessionHandlerImpl.getUserFullName(request) + " has " + action + " " + groupName + " item: from" + "( " + oldName + " to " + name + " )", request, "0");  //Audit trail entry improved for ERP-33281
                } else {
                    auditTrailObj.insertAuditLog(AuditAction.WAREHOUSE_CHANGED, "User " + sessionHandlerImpl.getUserFullName(request) + " has " + action +" "+ groupName+" item " + name, request, "0");
                }                
//                txnManager.commit(status);
            }

        } catch (Exception ex) {
//            txnManager.rollback(status);
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
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    
       public KwlReturnObject saveStoreMasterItem(HttpServletRequest request) throws SessionExpiredException, ServiceException {
        KwlReturnObject result = null;
        String msg = messageSource.getMessage("acc.master.save", null, RequestContextUtils.getLocale(request));   //"Master item has been saved successfully";
        boolean isPresent = false;
        HashMap requestParam = AccountingManager.getGlobalParams(request);
        boolean isEdit = StringUtil.isNullOrEmpty(request.getParameter("isEdit")) ? false : Boolean.parseBoolean(request.getParameter("isEdit"));
         String itemID = request.getParameter("id");
        if (isEdit) {
            requestParam.put("id", itemID);
        }
          String parentid=request.getParameter("parentid");
       requestParam.put("parentid", parentid);
        requestParam.put("name", request.getParameter("name"));
//        requestParam.put("parent", request.getParameter("parent"));
        String groupNm=request.getParameter("groupName");
        int type=0;
        if(groupNm.equals("Row")){
           type=1;
       }else if(groupNm.equals("Rack")){
           type=2;
       }else if(groupNm.equals("Bin")){
           type=3;
       }
       requestParam.put("type",type);
        HashMap<String, Object> filterRequestParams = new HashMap<String, Object>();
        ArrayList filter_names = new ArrayList(), filter_params = new ArrayList();
        filter_names.add("company.companyID");
        filter_params.add(requestParam.get("companyid"));
        filter_names.add("name");
        filter_names.add("type");
        filter_params.add(request.getParameter("name"));
        filter_params.add(type);
        filterRequestParams.put("filter_names", filter_names);
        filterRequestParams.put("filter_params", filter_params);
        KwlReturnObject cntResult = accMasterItemsDAOobj.getStoreMasters(filterRequestParams);
        int count = cntResult.getRecordTotalCount();

        if (count == 1) {
            String recordID = ((StoreMaster) cntResult.getEntityList().get(0)).getId();
            isPresent = itemID.equals(recordID) ? false : true; //Allow Editing same record
        } else if (count > 1) {
            isPresent = true;
        }

        if (isPresent) {
            msg = "<b>" + request.getParameter("name") + "</b>:" + messageSource.getMessage("acc.masteritem.entry.already.exist", null, RequestContextUtils.getLocale(request));
            return new KwlReturnObject(false, msg, null, null, 0);
        } else {
            result = accMasterItemsDAOobj.addUpdateStoreMasterItem(requestParam);
        }
        return new KwlReturnObject(true, msg, null, result.getEntityList(), 0);
    }
     public ModelAndView deleteStoreMasterItem(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false, isCommitEx = false;
        String msg = "";
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("MI_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        try {
            int no = deleteStoreMasterItem(request);
            issuccess = true;
            msg = messageSource.getMessage("acc.master.del", null, RequestContextUtils.getLocale(request));   //"Master item has been deleted successfully";
            try {
                String name[] = request.getParameterValues("name");
                String groupName = request.getParameter("groupName");
                for (int i = 0; i < name.length; i++) {
                    auditTrailObj.insertAuditLog(AuditAction.MASTER_GROUP, "User " + sessionHandlerImpl.getUserFullName(request) + " has deleted group " + groupName + " item " + name[i], request, "0");
                }
                txnManager.commit(status);
            } catch (Exception ex) {
                isCommitEx = true;
                msg = messageSource.getMessage("acc.master.excp1", null, RequestContextUtils.getLocale(request));   //"The Master Item(s) is or had been used in transaction(s). So, it cannot be deleted.";
            }
        } catch (ServiceException ex) {
            if (!isCommitEx) {
                txnManager.rollback(status);
            }
            msg = "" + ex.getMessage();
            Logger.getLogger(accMasterItemsController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            txnManager.rollback(status);
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
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public int deleteStoreMasterItem(HttpServletRequest request) throws ServiceException, AccountingException {
        String ids[] = request.getParameterValues("ids");
        int numRows = 0;
        try {
            for (int i = 0; i < ids.length; i++) {
                accMasterItemsDAOobj.deleteStoreMasterItem(ids[i]);
                numRows++;
            }
        } catch (ServiceException ex) {
            throw new AccountingException(messageSource.getMessage("acc.master.excp1", null, RequestContextUtils.getLocale(request)));
        }
        return numRows;
    }
     public ModelAndView getLLevelMappingFrmLevlId(HttpServletRequest request,HttpServletResponse response)throws ServiceException{
          JSONArray dataJArr = new JSONArray();
          JSONObject jobj=new JSONObject();
        boolean issuccess = false;
        String msg = "";
        try {
            String companyID = sessionHandlerImpl.getCompanyid(request);
            int levelId=Integer.parseInt(request.getParameter("levelId"));
            KwlReturnObject result = accMasterItemsDAOobj.getLLevelMappingFrmLevlId(companyID,levelId);
            List<Object> resultList = result.getEntityList();
            if (!resultList.isEmpty()) {
                for(Object resultObj:resultList){
                    JSONObject jSONObject=new JSONObject();
                    LocationLevelMapping locationLevelmap=(LocationLevelMapping)resultObj;
                    
                    jSONObject.put("Id", locationLevelmap.getID());
                    jSONObject.put("newLevelName", locationLevelmap.getNewLevelNm());
                    jSONObject.put("parent", locationLevelmap.getParent());
                    jSONObject.put("isActivate", locationLevelmap.isActivate());
                    dataJArr.put(jSONObject);
                }
            
            }
            jobj.put("data", dataJArr);        
        }catch (Exception ex) {
            throw ServiceException.FAILURE("crmManager.insertAccProduct", ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accMasterItemsController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
   
    
    public ModelAndView getPriceListCommonDiscount(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        try {
            jobj = getPriceListCommonDiscount(request);
            issuccess = true;
        } catch (SessionExpiredException ex) {
            msg = ex.getMessage();
            Logger.getLogger(accMasterItemsController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ServiceException ex) {
            msg = ex.getMessage();
            Logger.getLogger(accMasterItemsController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            msg = "accMasterItemsController.getPriceListCommonDiscount : " + ex.getMessage();
            Logger.getLogger(accMasterItemsController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accMasterItemsController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    
    public JSONObject getPriceListCommonDiscount(HttpServletRequest request) throws ServiceException, SessionExpiredException {
        JSONObject jobj = new JSONObject();
        try {
            boolean isPricePolicyUseDiscount = StringUtil.isNullOrEmpty(request.getParameter("isPricePolicyUseDiscount")) ? false : Boolean.parseBoolean(request.getParameter("isPricePolicyUseDiscount"));
            boolean useCommonDiscount = StringUtil.isNullOrEmpty(request.getParameter("useCommonDiscount")) ? false : Boolean.parseBoolean(request.getParameter("useCommonDiscount"));
            String companyID = sessionHandlerImpl.getCompanyid(request);
            DateFormat df = authHandler.getDateFormatter(request);
            String applicableDateStr = request.getParameter("applicableDate");
            Date applicableDate = null;
            if (!StringUtil.isNullOrEmpty(applicableDateStr)) {
                applicableDate = df.parse(applicableDateStr);
            }
            
            HashMap<String, Object> dataMap = new HashMap<String, Object>();
            dataMap.put("isPricePolicyUseDiscount", isPricePolicyUseDiscount);
            dataMap.put("useCommonDiscount", useCommonDiscount);
            dataMap.put("pricingBandMasterID", request.getParameter("pricingBandMasterID"));
            dataMap.put("applicableDate", applicableDate);
            dataMap.put("companyID", companyID);

            KwlReturnObject result = accMasterItemsDAOobj.getPriceOfProductForPricingBandAndCurrency(dataMap);

            if (result.getEntityList() != null && !result.getEntityList().isEmpty()) {
                String priceListVolumeDisocuntDetailID = result.getEntityList().get(0) != null ? (String) result.getEntityList().get(0) : "";
                
                if (!StringUtil.isNullOrEmpty(priceListVolumeDisocuntDetailID)) {
                    KwlReturnObject detailsResult = accountingHandlerDAOobj.getObject(PricingBandMasterDetail.class.getName(), priceListVolumeDisocuntDetailID);
                    PricingBandMasterDetail pricingBandMasterDetail = (PricingBandMasterDetail) detailsResult.getEntityList().get(0);
                    
                    jobj.put("minimumQty", pricingBandMasterDetail.getMinimumQty());
                    jobj.put("maximumQty", pricingBandMasterDetail.getMaximumQty());
                    jobj.put("discountType", pricingBandMasterDetail.getDiscountType() != null ? pricingBandMasterDetail.getDiscountType() : 0);
                    jobj.put("disocuntValue", pricingBandMasterDetail.getDiscountValue());
                }
            }
        } catch (Exception e) {
            throw ServiceException.FAILURE(e.getMessage(), e);
        }
        return jobj;
    }
    
    public void updateCommonDisocuntForAllProducts(HttpServletRequest request) throws ServiceException, SessionExpiredException {
        TransactionStatus status = null;
        try {
//            DateFormat df = authHandler.getDateFormatter(request);    //refer ticket ERP-15117
            DateFormat df = authHandler.getDateOnlyFormat(request);
            String companyID = sessionHandlerImpl.getCompanyid(request);
            int minimumQty = StringUtil.isNullOrEmpty(request.getParameter("minimumQty")) ? 0 : Integer.parseInt(request.getParameter("minimumQty"));
            int maximumQty = StringUtil.isNullOrEmpty(request.getParameter("maximumQty")) ? 0 : Integer.parseInt(request.getParameter("maximumQty"));
            String discountType = StringUtil.isNullOrEmpty(request.getParameter("discountType")) ? "" : request.getParameter("discountType");
            String disocuntValue = StringUtil.isNullOrEmpty(request.getParameter("disocuntValue")) ? "0" : request.getParameter("disocuntValue");
            String applicableDateStr = request.getParameter("applicableDate");
            Date applicableDate = null;
            if (!StringUtil.isNullOrEmpty(applicableDateStr)) {
                applicableDate = df.parse(applicableDateStr);
            }
            String currencyIDStr = request.getParameter("currency") != null ? request.getParameter("currency") : "";
            String[] currencyIDArr = currencyIDStr.split(",");
            
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            requestParams.put("companyID", companyID);

            KwlReturnObject result = accMasterItemsDAOobj.getProductsForPricingBandMasterDetails(requestParams);

            List<Object[]> list = result.getEntityList();
            int limit = Constants.Transaction_Commit_Limit;
            int count = 1;            
            DefaultTransactionDefinition def = new DefaultTransactionDefinition();
            def.setName("ProductCMN_Tx");
            def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
            status = txnManager.getTransaction(def);
            
            for (Object[] productObj : list) {
                for (int j = 0; j < currencyIDArr.length; j++) {
                    HashMap<String, Object> dataMap = new HashMap<String, Object>();
                    dataMap.put("isPricePolicyUseDiscount", true);
                    dataMap.put("pricingBandMasterID", request.getParameter("pricingBandMasterID"));
                    dataMap.put("applicableDate", applicableDate);
                    dataMap.put("isSavePricingBandMasterDetails", true);
                    dataMap.put("productID", productObj[0]);
                    dataMap.put("minimumQty", minimumQty);
                    dataMap.put("maximumQty", maximumQty);
                    dataMap.put("discountType", discountType);
                    dataMap.put("disocuntValue", disocuntValue);
                    dataMap.put("companyID", companyID);
                    dataMap.put("currencyID", currencyIDArr[j]);

                    KwlReturnObject resultOfPrice = accMasterItemsDAOobj.getPriceOfProductForPricingBandAndCurrency(dataMap);
                    if (resultOfPrice.getEntityList() != null && !resultOfPrice.getEntityList().isEmpty()) { // ignore if having discount for product
//                    continue;
                        List<String> priceList = resultOfPrice.getEntityList();
                        for (String priceVolObj : priceList) {
                            dataMap.put("rowid", priceVolObj);
                        }
                    }
                    accMasterItemsDAOobj.saveOrUpdatePricingBandMasterDetails(dataMap);

                    if (count == limit) {
                        txnManager.commit(status);
                        
                        count = 1;
                        def = new DefaultTransactionDefinition();
                        def.setName("ProductCMN_Tx");
                        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
                        status = txnManager.getTransaction(def);
                    } else {
                        count++;
                    }
                }
            }
            
            txnManager.commit(status);
        } catch (Exception ex) {
            if (status != null) {
                txnManager.rollback(status);
            }
            
            throw ServiceException.FAILURE("updateCommonDisocuntForAllProducts: " + ex.getMessage(), ex);
        }
    }
      public ModelAndView getLocItems(HttpServletRequest request, HttpServletResponse response) {
        JSONArray dataJArr = new JSONArray();
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        int levelId=0;
        String level=(String) request.getParameter("levelid");
        if(level!=null && !level.equals("")){
            levelId=Integer.parseInt(level);
        }
        try {
        switch(levelId){
            case 1:
                jobj = getWarehouseItems(request);
              break;
            case 2:
                jobj = getLocationItems(request);
              break;
            default:
                jobj = getStoreMasters(request);
        }
            issuccess = true;
        } catch (SessionExpiredException ex) {
            msg = ex.getMessage();
            Logger.getLogger(accMasterItemsController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } catch (ServiceException ex) {
            msg = ex.getMessage();
            Logger.getLogger(accMasterItemsController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } catch (Exception ex) {
            msg = "accMasterItemsController.getStoreMasters : " + ex.getMessage();
            Logger.getLogger(accMasterItemsController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accMasterItemsController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }    
      public ModelAndView chkStoreMasterSettingUsed(HttpServletRequest request, HttpServletResponse response) {
        JSONArray dataJArr = new JSONArray();
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        boolean isStoreMasterSettingUsed = false;
        String msg = "";
        
        try {
              String companyid=sessionHandlerImpl.getCompanyid(request);
              KwlReturnObject result=accMasterItemsDAOobj.isStoreMasterSettingUsed(companyid);
              if(result.getRecordTotalCount()==0)
                  isStoreMasterSettingUsed=true;
           issuccess = true;
        } catch (SessionExpiredException ex) {
            msg = ex.getMessage();
            Logger.getLogger(accMasterItemsController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } catch (ServiceException ex) {
            msg = ex.getMessage();
            Logger.getLogger(accMasterItemsController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } catch (Exception ex) {
            msg = "accMasterItemsController.chkLevelsUsed : " + ex.getMessage();
            Logger.getLogger(accMasterItemsController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } finally {
            try {
                jobj.put("isStoreMasterSettingUsed", isStoreMasterSettingUsed);
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accMasterItemsController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
      public ModelAndView sendAccMasterItemsToPOS(HttpServletRequest request, HttpServletResponse response) throws ServiceException {
        JSONObject jobj = new JSONObject();
        JSONObject pjobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        //Session session = null;
        try {
            String companyid = sessionHandlerImpl.getCompanyid(request);
            String subdomain = sessionHandlerImpl.getCompanySessionObj(request).getCdomain();
            String posURL = this.getServletContext().getInitParameter("posURL");
            JSONObject userData = new JSONObject();
            userData.put("iscommit", true);
            userData.put("remoteapikey", StorageHandler.GetRemoteAPIKey());
            userData.put("userid", sessionHandlerImpl.getUserid(request));
            userData.put("companyid", companyid);
            userData.put("subdomain", subdomain);
            String groupId=request.getParameter("groupid");
            String action =groupId.equals("57")?"38": "34";
            jobj = getMasterItems(request);
            JSONArray DataJArr = jobj.getJSONArray("data");
            pjobj.put("groupsData", DataJArr);
            pjobj.put("totalCount", DataJArr.length());
            pjobj.put("success", issuccess);
            pjobj.put("msg", msg);
            userData.put("data", pjobj);
            JSONObject resObj = new JSONObject();
            try {
                //session = HibernateUtil.getCurrentSession();
                resObj = apiCallHandlerService.callApp(posURL, userData, companyid, action);
            } catch (Exception e) {
                msg = e.getMessage();
            }
//            finally {
//                HibernateUtil.closeSession(session);
//            }

            if (!resObj.isNull("success") && resObj.getBoolean("success")) {
                issuccess = resObj.getBoolean("success");
                msg = resObj.getString("msg");
                jobj.put("success", true);
                jobj.put("msg", msg);
                jobj.put("companyexist", resObj.optBoolean("companyexist"));
            } else {
                issuccess = false;
                msg = resObj.getString("msg");
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            }
//            HibernateUtil.closeSession(session);
        } catch (Exception ex) {
            msg = ex.getMessage();
            Logger.getLogger(accMasterItemsController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accMasterItemsController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
     
    public ModelAndView getPricingBandMasterForProductDetails(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        JSONArray DataJArr = new JSONArray();
        boolean issuccess = false;
        String msg = "";
        try {
            boolean isVolumeDiscount = StringUtil.isNullOrEmpty(request.getParameter("isVolumeDiscount")) ? false : Boolean.parseBoolean(request.getParameter("isVolumeDiscount"));
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            requestParams.put("ss", request.getParameter("ss"));
            requestParams.put("start", request.getParameter("start"));
            requestParams.put("limit", request.getParameter("limit"));
            requestParams.put("companyid", sessionHandlerImpl.getCompanyid(request));

            KwlReturnObject result;
            if (isVolumeDiscount) {
                result = accMasterItemsDAOobj.getPriceListVolumeDiscountItems(requestParams);
            } else {
                result = accMasterItemsDAOobj.getPricingBandItems(requestParams);
            }

            List<PricingBandMaster> list = result.getEntityList();
            DataJArr = getPricingBandMasterForProductDetailsJson(request, list);
            int totalCount = result.getRecordTotalCount();

            jobj.put("data", DataJArr);
            jobj.put("totalCount", totalCount);
            issuccess = true;
        } catch (SessionExpiredException ex) {
            msg = ex.getMessage();
            Logger.getLogger(accMasterItemsController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ServiceException ex) {
            msg = ex.getMessage();
            Logger.getLogger(accMasterItemsController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
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
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    
      public ModelAndView activateDeactivateSalesperson(HttpServletRequest request, HttpServletResponse response){
        JSONObject jobj=new JSONObject();
        String msg="";
        boolean issuccess = false;
        KwlReturnObject result = null;
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("Account_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        try {
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            requestParams.put("data", request.getParameterValues("data"));
            String customerActivateDeactivate = request.getParameter("activateDeactivateFlag");
            String companyid=sessionHandlerImpl.getCompanyid(request);
            boolean salespersonActivateDeactivateFlag = StringUtil.isNullOrEmpty(customerActivateDeactivate)?false:Boolean.parseBoolean(customerActivateDeactivate);
            requestParams.put("salespersonActivateDeactivateFlag", salespersonActivateDeactivateFlag);
            requestParams.put("companyid", companyid);
            result = accMasterItemsDAOobj.activateDeactivateSalesperson(requestParams);
            issuccess = true;
            msg = salespersonActivateDeactivateFlag ? messageSource.getMessage("acc.master.configuration.salesperson.activated", null, RequestContextUtils.getLocale(request)) : messageSource.getMessage("acc.master.configuration.salesperson.deactivated", null, RequestContextUtils.getLocale(request));
            txnManager.commit(status);

            auditMsg = salespersonActivateDeactivateFlag ? " has Activated Sales Person " : " has Deactivated Sales Person ";
            for (int i = 0; i < result.getRecordTotalCount(); i++) {
                MasterItem masteritemObj = (MasterItem) result.getEntityList().get(i);
                auditTrailObj.insertAuditLog(AuditAction.SALESPERSON_ACTIVATE_DEACTIVATE, "User " + sessionHandlerImpl.getUserFullName(request) + auditMsg + "<b>" + masteritemObj.getValue() + "</b>" + " ( " + masteritemObj.getCode() + " ) ", request, masteritemObj.getID());
            }
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = ""+ex.getMessage();
            Logger.getLogger(accMasterItemsController.class.getName()).log(Level.SEVERE, null, ex);
        } finally{
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accMasterItemsController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public JSONArray getPricingBandMasterForProductDetailsJson(HttpServletRequest request, List<PricingBandMaster> list) throws JSONException, ServiceException {
        JSONArray jArr = new JSONArray();
        try {
            boolean isVolumeDiscount = StringUtil.isNullOrEmpty(request.getParameter("isVolumeDiscount")) ? false : Boolean.parseBoolean(request.getParameter("isVolumeDiscount"));
            String companyID = sessionHandlerImpl.getCompanyid(request);
            DateFormat df = authHandler.getDateFormatter(request);
            String applicableDateStr = request.getParameter("applicableDate");
            Date applicableDate = null;
            if (!StringUtil.isNullOrEmpty(applicableDateStr)) {
                applicableDate = df.parse(applicableDateStr);
            }

            for (PricingBandMaster row : list) {

                HashMap<String, Object> requestParams = new HashMap<String, Object>();
                requestParams.put("pricingBandMasterID", row.getID());
                requestParams.put("applicableDate", applicableDate);
                requestParams.put("productID", request.getParameter("productID"));
                requestParams.put("companyID", companyID);
                KwlReturnObject result = accMasterItemsDAOobj.getPriceOfBandForProductAndCurrency(requestParams);
                List<Object[]> pricingBandDetailsList = result.getEntityList();

                for (Object[] priceObj : pricingBandDetailsList) {
                    KwlReturnObject pricingBandMasterDetailResult = accountingHandlerDAOobj.getObject(PricingBandMasterDetail.class.getName(), (String) priceObj[2]);
                    PricingBandMasterDetail pricingBandMasterDetail = (PricingBandMasterDetail) pricingBandMasterDetailResult.getEntityList().get(0);

                    JSONObject obj = new JSONObject();
                    obj.put("bandUUID", row.getID());
                    obj.put("bandName", row.getName());

                    if (isVolumeDiscount) {
                        obj.put("desc", row.getDescription());
                        obj.put("pricePolicy", row.getPricePolicyValue());
                        obj.put("minimumQty", pricingBandMasterDetail.getMinimumQty());
                        obj.put("maximumQty", pricingBandMasterDetail.getMaximumQty());
                    }

                    if (row.getPricePolicyValue() != 1) {
                        obj.put("purchasePrice", pricingBandMasterDetail.getPurchasePrice());
                        obj.put("salesPrice", pricingBandMasterDetail.getSalesPrice());
                    } else {
                        if (pricingBandMasterDetail.getDiscountType().equalsIgnoreCase("0")) {
                            obj.put("discountType", "Flat");
                        } else {
                            obj.put("discountType", "Percentage");
                        }
                        obj.put("disocuntValue", pricingBandMasterDetail.getDiscountValue());
                    }

                    obj.put("applicableDate", df.format(pricingBandMasterDetail.getApplicableDate()));
                    obj.put("currencyName", pricingBandMasterDetail.getCurrency() != null ? pricingBandMasterDetail.getCurrency().getName() : "");
                    obj.put("currencysymbol", pricingBandMasterDetail.getCurrency() != null ? pricingBandMasterDetail.getCurrency().getSymbol() : "");

                    jArr.put(obj);
                }
            }
        } catch (Exception ex) {
            Logger.getLogger(accMasterItemsController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return jArr;
    }

    public JSONArray getIBGReceivingBankDetails(List IBGReceivingBankDetailsList) throws JSONException {
        JSONArray returnArray = new JSONArray();
        if (!IBGReceivingBankDetailsList.isEmpty()) {
            Iterator it = IBGReceivingBankDetailsList.iterator();
            while (it.hasNext()) {
                JSONObject jobj = new JSONObject();
                Object[] tempobj = (Object[]) it.next();
                jobj.put("ibgId", tempobj[0]);
                jobj.put("receivingBankCode", tempobj[1]);
                jobj.put("receivingBankName", tempobj[2]);
                jobj.put("receivingBranchCode", tempobj[3]);
                jobj.put("receivingAccountNumber", tempobj[4]);
                jobj.put("receivingAccountName", tempobj[5]);

                returnArray.put(jobj);
            }
        }
        return returnArray;
    }
    
    public HashMap<String, Object> getIBGReceivingBankDetailsRequestParamsMap(HttpServletRequest request) throws SessionExpiredException {

        HashMap<String, Object> requestParams = new HashMap<String, Object>();

        requestParams.put("receivingBankDetailId", request.getParameter("receivingBankDetailId"));

        requestParams.put("receivingBankCode", request.getParameter("receivingBankCode"));

        requestParams.put("receivingBankName", request.getParameter("receivingBankName"));

        requestParams.put("receivingBranchCode", request.getParameter("receivingBranchCode"));

        requestParams.put("receivingAccountNumber", request.getParameter("receivingAccountNumber"));

        requestParams.put("receivingAccountName", request.getParameter("receivingAccountName"));

        requestParams.put("vendorId", request.getAttribute("vendorId") != null ? (String) request.getAttribute("vendorId") : request.getParameter("vendor"));

        requestParams.put("masterItemId", request.getAttribute("masterItemId") != null ? (String) request.getAttribute("masterItemId") : request.getParameter("masterItem"));

        requestParams.put("companyId", sessionHandlerImpl.getCompanyid(request));

        return requestParams;
    }
    
    public JSONArray getCIMBReceivingBankDetails(HttpServletRequest request) throws SessionExpiredException, ServiceException {
        JSONArray returnArray = new JSONArray();
        try {
            HashMap<String, Object> requestParams = getCIMBReceivingBankDetailsRequestParamsMap(request);

            KwlReturnObject returnObject = accMasterItemsDAOobj.getCIMBReceivingBankDetails(requestParams);

            if (returnObject != null && !returnObject.getEntityList().isEmpty()) {
                HashMap<String, Object> requestParamsForPayment = AccountingManager.getGlobalParams(request);
                returnArray = getCIMBReceivingBankDetails(returnObject.getEntityList(),requestParamsForPayment);
            }

        } catch (ServiceException ex) {
            Logger.getLogger(accMasterItemsController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            throw ServiceException.FAILURE("getIBGReceivingBankDetailsForVendor : " + ex.getMessage(), ex);
        } catch (JSONException ex) {
            Logger.getLogger(accMasterItemsController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            throw ServiceException.FAILURE("getIBGReceivingBankDetailsForVendor : " + ex.getMessage(), ex);
        }
        return returnArray;
    }
    
    public HashMap<String, Object> getCIMBReceivingBankDetailsRequestParamsMap(HttpServletRequest request) throws SessionExpiredException {

        HashMap<String, Object> requestParams = new HashMap<String, Object>();
        
        requestParams.put("CIMBReceivingBankDetailId", request.getParameter("CIMBReceivingBankDetailId"));

        requestParams.put("collectionAccNo", request.getParameter("collectionAccNo"));

        requestParams.put("collectionAccName", request.getParameter("collectionAccName"));

        requestParams.put("giroBICCode", request.getParameter("giroBICCode"));

        requestParams.put("refNumber", request.getParameter("refNumber"));

        requestParams.put("vendorId", request.getAttribute("vendorId")!=null?(String)request.getAttribute("vendorId"): request.getParameter("vendor"));

        requestParams.put("masterItemId", request.getAttribute("masterItem") != null ? (String)request.getAttribute("masterItem") : request.getParameter("masterItem"));

        requestParams.put("companyId", sessionHandlerImpl.getCompanyid(request));

        return requestParams;
    }
    public JSONArray getCIMBReceivingBankDetails(List CIMBReceivingBankDetailsList, HashMap requestParamsForPayment) throws JSONException, ServiceException {
        JSONArray returnArray = new JSONArray();
        if (!CIMBReceivingBankDetailsList.isEmpty()) {
            Iterator it = CIMBReceivingBankDetailsList.iterator();
            while (it.hasNext()) {
                JSONObject jobj = new JSONObject();
                Object[] tempobj = (Object[]) it.next();
                jobj.put("ibgId", tempobj[0]);
                jobj.put("collectionAccountNumber", tempobj[1]);
                jobj.put("collectionAccountName", tempobj[2]);
                jobj.put("giroBICCode", tempobj[3]);
                jobj.put("referenceNumber", tempobj[4]);
                jobj.put("emailForGiro", tempobj[5]);
                requestParamsForPayment.put("cimbReceivingBankDetailId", tempobj[0]);                
                KwlReturnObject result = accMasterItemsDAOobj.getPaymentsWithCimb(requestParamsForPayment);
                List list = result.getEntityList();
                if(!list.isEmpty()){
                    jobj.put("usedInPayment", true);
                }
                returnArray.put(jobj);
            }
        }
        return returnArray;
    }
    
    /**
     * Description: Method is used to copy all band details to new band
     * @param <requestMap> need key 'existingBandID' and 'newBandID'
     */
    public void copyPricingBandDetails(Map<String, Object> requestMap) throws ServiceException {
        try {
            HashMap<String, Object> filterRequestParams = new HashMap<>();
            ArrayList filter_names = new ArrayList(), filter_params = new ArrayList();
            filter_names.add("pricingBandMaster.ID");
            filter_params.add(requestMap.get("existingBandID"));
            filterRequestParams.put("filter_names", filter_names);
            filterRequestParams.put("filter_params", filter_params);
            KwlReturnObject detailObj = accMasterItemsDAOobj.getPricingBandMasterDetailsList(filterRequestParams);
            List<PricingBandMasterDetail> detailList = detailObj.getEntityList();
            
            for (PricingBandMasterDetail row : detailList) {
                HashMap<String, Object> dataMap = new HashMap<>();
                dataMap.put("pricingBandMasterID", requestMap.get("newBandID"));
                dataMap.put("currencyID", (row.getCurrency() != null) ? row.getCurrency().getCurrencyID() : "");
                dataMap.put("productID", row.getProduct());
                dataMap.put("purchasePrice", String.valueOf(row.getPurchasePrice()));
                dataMap.put("salesPrice", String.valueOf(row.getSalesPrice()));
                dataMap.put("companyID", (row.getCompany() != null) ? row.getCompany().getCompanyID() : "");
                dataMap.put("applicableDate", row.getApplicableDate());
                
                accMasterItemsDAOobj.saveOrUpdatePricingBandMasterDetails(dataMap);
            }
        } catch (Exception ex) {
            Logger.getLogger(accMasterItemsController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            throw ServiceException.FAILURE("copyPricingBandDetails : " + ex.getMessage(), ex);
        }
    }
    public ModelAndView sendProcessAndSkillToPM(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean success = false;
        try {
            /*
             Below function is used to Process and skills
             */
            Map<String, Object> requestParams = new HashMap<String, Object>();
            requestParams.put("companyId", sessionHandlerImpl.getCompanyid(request));
            requestParams.put("userId", sessionHandlerImpl.getUserid(request));
            JSONObject jSONObject = accMasterItemsService.sendProcessSkill(requestParams);
            success = true;
            msg = messageSource.getMessage("acc.master.configuration.Process.Sync.Success", null, RequestContextUtils.getLocale(request));
        } catch (Exception ex) {
            msg = ex.getMessage();
        } finally {
            try {
                jobj.put(Constants.RES_success, success);
                jobj.put(Constants.RES_msg, msg == null ? "null" : msg);
            } catch (JSONException ex) {
                Logger.getLogger(accMasterItemsController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView(Constants.jsonView, Constants.model, jobj.toString());
    }
    
    /*
     * Fetch only those batches which are used in Build Assembly - BOM Components
     */
    public ModelAndView getUsedBatchesForAssembly(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        try {
            jobj = getUsedBatches(request);
            issuccess = true;
        } catch (SessionExpiredException ex) {
            msg = ex.getMessage();
            Logger.getLogger(accMasterItemsController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ServiceException ex) {
            msg = ex.getMessage();
            Logger.getLogger(accMasterItemsController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            msg = "accMasterItemsController.getUsedBatchesForAssembly : " + ex.getMessage();
            Logger.getLogger(accMasterItemsController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accMasterItemsController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public JSONObject getUsedBatches(HttpServletRequest request) throws ServiceException, SessionExpiredException {
        JSONObject jobj = new JSONObject();
        try {
            String productid = request.getParameter("mainproduct");
            String warehouse = request.getParameter("warehouse");
            String location = request.getParameter("location");
            String subproduct = !StringUtil.isNullOrEmpty(request.getParameter("productid")) ? request.getParameter("productid") : null;
            String companyId = sessionHandlerImpl.getCompanyid(request);
            JSONArray jArr = new JSONArray();
            HashMap<String, Object> map = new HashMap<String, Object>();
            map.put("productid", productid);
            map.put("subproduct", subproduct);
            map.put("companyid", companyId);
            map.put("warehouse", warehouse);
            map.put("location", location);
            List list = accMasterItemsDAOobj.getUsedBatchAssemblyProduct(map);
            Iterator itr = null;
            if (list != null && !list.isEmpty()) {
                itr = list.iterator();
                while (itr.hasNext()) {
                    JSONObject job = new JSONObject();
                    JSONObject row = (JSONObject) itr.next();
                    job.put("id", row.getString("id"));
                    job.put("batchname", row.getString("batchname"));
                    job.put("productid", row.getString("productid"));
                    job.put("warehouse", row.getString("warehouse"));
                    job.put("location", row.getString("location"));
                    job.put("row", row.getString("row"));
                    job.put("rack", row.getString("rack"));
                    job.put("bin", row.getString("bin"));
                    job.put("mfgdate", (row.get("mfgdate") != null && row.get("mfgdate") != "") ? (Date) row.get("mfgdate") : "");
                    job.put("expdate", (row.get("expdate") != null && row.get("expdate") != "") ? (Date) row.get("expdate") : "");
                    job.put("batch", row.getString("batch"));   //Don't know the use of this field
                    jArr.put(job);
                }
            }
            jobj.put("data", jArr);
        } catch (JSONException e) {
            throw ServiceException.FAILURE(e.getMessage(), e);
        }
        return jobj;
    }
        
    /*
     * Fetch only those serial which are used in Build Assembly - BOM Components with respective batch
     */
    public ModelAndView getUsedSerialsForAssembly(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        try {
            jobj = getUsedSerial(request);
            issuccess = true;
        } catch (SessionExpiredException ex) {
            msg = ex.getMessage();
            Logger.getLogger(accMasterItemsController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ServiceException ex) {
            msg = ex.getMessage();
            Logger.getLogger(accMasterItemsController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            msg = "accMasterItemsController.getUsedSerialsForAssembly : " + ex.getMessage();
            Logger.getLogger(accMasterItemsController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accMasterItemsController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public JSONObject getUsedSerial(HttpServletRequest request) throws ServiceException, SessionExpiredException {
        JSONObject jobj = new JSONObject();
        try {
            DateFormat df = authHandler.getDateOnlyFormat(request);
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            String productid = request.getParameter("mainproduct"); //Parent Product
            String subproduct = !StringUtil.isNullOrEmpty(request.getParameter("productid")) ? request.getParameter("productid") : null;    //Sub ProductID
            String refno = !StringUtil.isNullOrEmpty(request.getParameter("refno")) ? request.getParameter("refno") : "";
            String batch = !StringUtil.isNullOrEmpty(request.getParameter("batch")) ? request.getParameter("batch") : "";
            String companyId = sessionHandlerImpl.getCompanyid(request);
            JSONArray jArr = new JSONArray();
            HashMap<String, Object> map = new HashMap<String, Object>();
            map.put("productid", productid);
            map.put("subproduct", subproduct);
            map.put("companyid", companyId);
            map.put("batch", batch);
            map.put("refno", refno);
            List list = accMasterItemsDAOobj.getUsedSerialNoAssemblyProduct(map);
            Iterator itr = null;
            if (list != null && !list.isEmpty()) {
                itr = list.iterator();
                while (itr.hasNext()) {
                    JSONObject job = new JSONObject();
                    JSONObject row = (JSONObject) itr.next();
                    job.put("id", row.getString("id"));
                    job.put("serialno", row.getString("serialno"));
                    job.put("serialnoid", row.getString("serialnoid"));
                    job.put("expstart", row.get("expstart"));
                    job.put("expend", row.get("expend"));
                    job.put("skufield", row.get("skufield"));
                    job.put("product", row.get("product"));
                    job.put("purchaseserialid", row.get("purchaseserialid"));
                    job.put("purchasebatchid", row.get("purchasebatchid"));
                    jArr.put(job);
                }
            }
            jobj.put("data", jArr);
        } catch (JSONException e) {
            throw ServiceException.FAILURE(e.getMessage(), e);
        }
        return jobj;
    }
    
    
    
    public ModelAndView saveLandingCostOfCategory(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("MI_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        try {
            KwlReturnObject result = saveLandingCostOfCategory(request);
            issuccess = true;
            msg = result.getMsg();
            txnManager.commit(status);

        } catch (Exception ex) {
            txnManager.rollback(status);
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
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    public KwlReturnObject saveLandingCostOfCategory(HttpServletRequest request) throws SessionExpiredException, ServiceException {
        KwlReturnObject result = null;
        boolean issuccess = false; 
        boolean isEdit = false; 
        List resultList=null;
        String msg = messageSource.getMessage("acc.master.save", null, RequestContextUtils.getLocale(request));   //"Master item has been saved successfully";
        HashMap requestParam = AccountingManager.getGlobalParams(request);
        String itemID = request.getParameter("id");
        String companyid = "";
        if(!StringUtil.isNullOrEmpty(itemID)){
            requestParam.put("itemID", itemID);
        }
        if(requestParam.containsKey(Constants.companyKey) && requestParam.get(Constants.companyKey)!=null && !StringUtil.isNullOrEmpty((String)requestParam.get(Constants.companyKey))){
           companyid=(String)requestParam.get(Constants.companyKey);
        }
        if (!StringUtil.isNullOrEmpty(request.getParameter("landingcostallocationtype"))) {
            requestParam.put("lcallocationid", (request.getParameter("landingcostallocationtype")));
        }
        if (!StringUtil.isNullOrEmpty(request.getParameter("landingcostcategory"))) {
            requestParam.put("lccategory", request.getParameter("landingcostcategory"));
        }
        if (!StringUtil.isNullOrEmpty(request.getParameter("isEdit"))) {
            isEdit=Boolean.valueOf(request.getParameter("isEdit"));
        }
        int count=0;
        if(!isEdit){           
            KwlReturnObject resultChk=accMasterItemsDAOobj.checkLandingCostCategoryRec(requestParam);
            count =resultChk!=null?resultChk.getRecordTotalCount():0;
        }
        
        if(count==0 || isEdit){
            result = accMasterItemsDAOobj.addLandingCostOfCategory(requestParam);           
            resultList=result!=null?result.getEntityList():null;
            issuccess=true;
        }else{
            msg="Duplicate Records cannot be created";
            issuccess=true;
        }
        

        return new KwlReturnObject(issuccess, msg, null,resultList, 0);

    }
    /**
     * @Desc : Save User GRP
     * @param request
     * @param response
     * @return
     */
    public ModelAndView saveUserGroup(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("Account_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        try {
            JSONObject reqParams = StringUtil.convertRequestToJsonObject(request);
            reqParams.put("companyid", sessionHandlerImpl.getCompanyid(request));
            reqParams.put("userid", sessionHandlerImpl.getUserid(request));
            reqParams.put(Constants.userfullname, sessionHandlerImpl.getUserFullName(request));
            jobj = accMasterItemsService.saveUserGroup(reqParams);
            issuccess = true;
            txnManager.commit(status);
            msg = messageSource.getMessage("acc.user.savemsg", null, RequestContextUtils.getLocale(request));
        } catch (SessionExpiredException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            Logger.getLogger(accMasterItemsController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = "" + ex.getMessage();
            if (ex.getMessage() == null) {
                msg = ex.getCause().getMessage();
            }
            Logger.getLogger(accMasterItemsController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accMasterItemsController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    /**
     * @Desc : Get Users GRP
     * @param request
     * @param response
     * @return
     */
    public ModelAndView getUsersGroup(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean success = false;
        try {
            /*
             Below function is used to create column model for grid
             */
            JSONObject reqparams = new JSONObject();
            reqparams.put("companyid", sessionHandlerImpl.getCompanyid(request));
            jobj = accMasterItemsService.getUsersGroup(reqparams);
            success = true;
        } catch (Exception ex) {
            msg = ex.getMessage();
        } finally {
            try {
                jobj.put(Constants.RES_success, success);
                jobj.put(Constants.RES_msg, msg == null ? "null" : msg);
            } catch (JSONException ex) {
                Logger.getLogger(accMasterItemsController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView(Constants.jsonView, Constants.model, jobj.toString());
    }

    /**
     * @Desc : Delete Users GRP
     * @param request
     * @param response
     * @return
     * @throws SessionExpiredException
     */
    public ModelAndView deleteUsersGroup(HttpServletRequest request, HttpServletResponse response) throws SessionExpiredException {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("JEC_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        try {
            /*
             Delete Users Group
             */

            JSONObject jSONObject = StringUtil.convertRequestToJsonObject(request);
            jSONObject.put("companyid", sessionHandlerImpl.getCompanyid(request));
            jSONObject.put("userid", sessionHandlerImpl.getUserid(request));
            jSONObject.put(Constants.userfullname, sessionHandlerImpl.getUserFullName(request));
            accMasterItemsService.deleteUsersGroup(jSONObject);
            txnManager.commit(status);
            issuccess = true;
            msg = messageSource.getMessage("acc.user.deletegrpmsg", null, RequestContextUtils.getLocale(request));
        } catch (Exception ex) {
            txnManager.rollback(status);
            issuccess = false;
            msg = messageSource.getMessage("acc.user.usedintransaction", null, RequestContextUtils.getLocale(request));
            Logger.getLogger(accMasterItemsController.class.getName()).log(Level.SEVERE, "accMasterItemsController.deleteUsersGroup", ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accMasterItemsController.class.getName()).log(Level.SEVERE, "accMasterItemsController.deleteUsersGroup", ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    /**
     * @Desc : Save User GRP and FCD Mapping
     * @param request
     * @param response
     * @return
     */
    public ModelAndView saveUserGroupFieldComboMapping(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jObj = new JSONObject();
        boolean isSuccess = false;
        String msg = "";
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("Account_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        try {
            JSONObject jSONObject = StringUtil.convertRequestToJsonObject(request);
            jSONObject.put("companyid", sessionHandlerImpl.getCompanyid(request));
            accMasterItemsService.saveUserGroupFieldComboMapping(jSONObject);
            accMasterItemsService.saveUserGroupFieldComboMappingForchild(jSONObject);
            txnManager.commit(status);
            isSuccess = true;
            msg = messageSource.getMessage("acc.user.savemsgfcdmapping", null, RequestContextUtils.getLocale(request));
        } catch (Exception ex) {
            txnManager.rollback(status);
            isSuccess = false;
            msg = ex.getMessage();
            Logger.getLogger(accMasterItemsController.class.getName()).log(Level.SEVERE, "accMasterItemsController.deleteUsersGroup", ex);
        } finally {
            try {
                jObj.put("isSuccess", isSuccess);
                jObj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accMasterItemsController.class.getName()).log(Level.SEVERE, "accMasterItemsController.deleteUsersGroup", ex);
            }
        }
        return new ModelAndView("jsonView", "model", jObj.toString());
    }
}

