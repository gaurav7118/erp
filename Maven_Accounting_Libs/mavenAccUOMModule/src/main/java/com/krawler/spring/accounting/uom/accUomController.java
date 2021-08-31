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
package com.krawler.spring.accounting.uom;

import com.krawler.common.admin.AuditAction;
import com.krawler.common.admin.ImportLog;
import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.common.util.Constants;
import com.krawler.common.util.StringUtil;
import com.krawler.hql.accounting.*;
import com.krawler.spring.accounting.handler.AccountingManager;
import com.krawler.spring.accounting.uom.service.AccUomService;
import com.krawler.spring.auditTrailModule.auditTrailDAO;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.spring.importFunctionality.ImportDAO;
import com.krawler.spring.importFunctionality.ImportHandler;
import com.krawler.spring.sessionHandler.sessionHandlerImpl;
import com.krawler.spring.storageHandler.storageHandlerImpl;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.io.*;
import java.math.BigInteger;
import java.net.URLDecoder;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceAware;
import org.springframework.context.NoSuchMessageException;
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
public class accUomController extends MultiActionController implements MessageSourceAware {

    private HibernateTransactionManager txnManager;
    private accUomDAO accUomObj;
    private String successView;
    private auditTrailDAO auditTrailObj;
    private MessageSource messageSource;
    public ImportHandler importHandler;
    private ImportDAO importDao;
    private AccUomService accUomService;
    
    @Override
    public void setMessageSource(MessageSource ms) {
        this.messageSource = ms;
    }

    public void setTxnManager(HibernateTransactionManager txManager) {
        this.txnManager = txManager;
    }

    public void setaccUomDAO(accUomDAO accUomObj) {
        this.accUomObj = accUomObj;
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
    
    public void setimportHandler(ImportHandler importHandler) {
        this.importHandler = importHandler;
    }
    
    public void setimportDAO(ImportDAO importDao) {
        this.importDao = importDao;
    }
    
    public void setaccUomService(AccUomService accUomService) {
        this.accUomService = accUomService;
    }
    
    public ModelAndView getUnitOfMeasure(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        try {
            HashMap<String, Object> requestParams = AccountingManager.getGlobalParams(request);
            String start = request.getParameter(Constants.start);
            String limit = request.getParameter(Constants.limit);
            boolean doNotShowNAUomName = !StringUtil.isNullOrEmpty(request.getParameter("doNotShowNAUomName")) ? Boolean.parseBoolean(request.getParameter("doNotShowNAUomName")) : false;
            if (doNotShowNAUomName) {
                requestParams.put("doNotShowNAUomName", doNotShowNAUomName);
            }

            KwlReturnObject result = accUomObj.getUnitOfMeasure(requestParams);
            List list = result.getEntityList();
            int count = result.getRecordTotalCount();

            JSONArray DataJArr = getUoMJson(request, list);
            
            JSONArray pagedJson = DataJArr;
            if (!StringUtil.isNullOrEmpty(start) && !StringUtil.isNullOrEmpty(limit)) {
                pagedJson = StringUtil.getPagedJSON(pagedJson, Integer.parseInt(start), Integer.parseInt(limit));
            }
            jobj.put("data", pagedJson);
            jobj.put("count", DataJArr.length());
            issuccess = true;
        } catch (SessionExpiredException ex) {
            msg = ex.getMessage();
            Logger.getLogger(accUomController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ServiceException ex) {
            msg = ex.getMessage();
            Logger.getLogger(accUomController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            msg = "" + ex.getMessage();
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accUomController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    
    public ModelAndView getDisplayUnitOfMeasure(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        try {
            JSONObject obj = new JSONObject();
            String start = request.getParameter(Constants.start);
            String limit = request.getParameter(Constants.limit);
            if(!StringUtil.isNullOrEmpty(request.getParameter("uomschematypeid"))){
                obj.put("uomschematypeid",request.getParameter("uomschematypeid"));
            } 
            KwlReturnObject result = accUomObj.getDisplayUnitOfMeasure(obj);
            List<UOMSchema> list = result.getEntityList();
            JSONArray jArr = accUomObj.getDisplayUOM(list);
            JSONArray pagedJson = jArr;
            if (!StringUtil.isNullOrEmpty(start) && !StringUtil.isNullOrEmpty(limit)) {
                pagedJson = StringUtil.getPagedJSON(pagedJson, Integer.parseInt(start), Integer.parseInt(limit));
            }
            jobj.put("data", pagedJson);
            jobj.put("count", jArr.length());
            issuccess = true;
        } catch (ServiceException ex) {
            msg = ex.getMessage();
            Logger.getLogger(accUomController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            msg = "" + ex.getMessage();
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accUomController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public JSONArray getUoMJson(HttpServletRequest request, List list) throws ServiceException {
        JSONArray jArr = new JSONArray();
        try {
            Iterator itr = list.iterator();
            while (itr.hasNext()) {
                UnitOfMeasure uom = (UnitOfMeasure) itr.next();
                    JSONObject obj = new JSONObject();
                    obj.put("uomid", uom.getID());
                    obj.put("uomname", uom.getName());
                    obj.put("precision", uom.getAllowedPrecision());
                    obj.put("uomtype", uom.getType());
                    obj.put("defaultunitofmeasure", uom.getDefaultunitofmeasure() != null ? uom.getDefaultunitofmeasure().getID() : "");
                    jArr.put(obj);
                }
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("getUoMJson : " + ex.getMessage(), ex);
        }
        return jArr;
    }

    public ModelAndView saveUnitOfMeasure(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;

        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("UoM_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);

        TransactionStatus status = txnManager.getTransaction(def);
        try {
            msg = saveUOM(request);
            
            issuccess = true;
            if(!StringUtil.isNullOrEmpty(msg)){
//                msg = msg.substring(0, (msg.lastIndexOf(",")));
                msg = messageSource.getMessage( "acc.uom.update", null, RequestContextUtils.getLocale(request))+"<br> <b> Except&nbsp: </b>Following <br>"+msg;
            }else{
                msg = messageSource.getMessage("acc.uom.update", null, RequestContextUtils.getLocale(request));   //"Unit of measure has been updated successfully";
            }
            
            txnManager.commit(status);
        } catch (SessionExpiredException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            Logger.getLogger(accUomController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = "" + ex.getMessage();
            Logger.getLogger(accUomController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accUomController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public String saveUOM(HttpServletRequest request) throws ServiceException, AccountingException, SessionExpiredException {
       String msg="";
        try {
            JSONArray jArr = new JSONArray(request.getParameter("data"));
            JSONArray jDelArr = new JSONArray(request.getParameter("deleteddata"));
            String companyid = sessionHandlerImpl.getCompanyid(request);
            String uomid = "";
            List uomList=new ArrayList();
            KwlReturnObject result;
            int delCount = 0;
            String uomNo="";
            for (int i = 0; i < jDelArr.length(); i++) {
                JSONObject jobj = jDelArr.getJSONObject(i);
                if (StringUtil.isNullOrEmpty(jobj.getString("uomid")) == false) {
                    uomid = jobj.getString("uomid");
                    String methodname = jobj.getString("uomname");
                    String moduleName="";
                    try {
                        result = accUomObj.searchUoM(uomid, companyid);
                        List<Object[]> list=result!= null ? result.getEntityList():null;
                        int count=0;
                        for(Object[] obj:list){
                            BigInteger bigInt=(BigInteger) obj[0];
                            count +=bigInt.intValue();
                            if (bigInt.intValue() > 0) {
                                moduleName += (String)obj[1] + ",";
                            }
                        }
                        if (!StringUtil.isNullOrEmpty(moduleName)) {
                            moduleName = moduleName.substring(0, (moduleName.length() - 1));
                        }
                        /*
                         * Delete the entry in Packaging that only use in packaging not in any of transaction.
                         */
                        if(count == 1 && !StringUtil.isNullOrEmpty(moduleName) && moduleName.equalsIgnoreCase("Packaging")){
                            Map<String, String> deletePackingMap = new HashMap<>();
                            deletePackingMap.put("stockuom", uomid);
                            deletePackingMap.put("companyid", companyid);
                            accUomObj.deletePackaging(deletePackingMap);
                            result = accUomObj.deleteUoM(uomid, companyid);
                        } else {
                            //int count = result.getRecordTotalCount();
                            if (count > 0) {
                                if (!uomList.contains(methodname)) {
                                    uomList.add(methodname);
                                    msg += methodname +" "+ messageSource.getMessage("acc.uom.msg", null, RequestContextUtils.getLocale(request)) +" "+ moduleName +"<br>";
                                }
                                continue;
                            } else {
                                if(!methodname.equals("N/A")){
                                    result = accUomObj.deleteUoM(uomid, companyid);
                                }else{
                                    throw new AccountingException(messageSource.getMessage("acc.uom.deleteexcp", null, RequestContextUtils.getLocale(request)));
                                }
                            }
                        }
                        delCount += result.getRecordTotalCount();
                        auditTrailObj.insertAuditLog(AuditAction.UNIT_OF_MEASURE_DELETED, "User " + sessionHandlerImpl.getUserFullName(request) + " has deleted unit of measure " + methodname, request, uomid + "0");
                    } catch (ServiceException ex) {
                        throw new AccountingException(messageSource.getMessage("acc.uom.excp1", null, RequestContextUtils.getLocale(request)));
                    }
                }
            }
            String auditMsg;
            String auditID;
            UnitOfMeasure uom;
            KwlReturnObject uomresult;
            HashMap<String, Object> uomMap;
//            String fullName = AuthHandler.getFullName(session, AuthHandler.getUserid(request));
//            if (delCount > 0) {
//                auditTrailObj.insertAuditLog(AuditAction.UNIT_OF_MEASURE_DELETED, "User " + sessionHandlerImpl.getUserFullName(request) + " deleted unit of measure "+methodname, request, uomid +"0");
//            }
            for (int i = 0; i < jArr.length(); i++) {
                JSONObject jobj = jArr.getJSONObject(i);
                if (jobj.getBoolean("modified") == false) {
                    continue;
                }
                if (jobj.optBoolean("modified")) {
                    String defaultunitofmeasure = jobj.optString("defaultunitofmeasure", "");
                    if (defaultunitofmeasure.equals(Constants.NA_UOM_DEFAULTMEASUREOFUOM_ID)){
                        throw new AccountingException(messageSource.getMessage("acc.uom.modifyexcp", null, RequestContextUtils.getLocale(request)));
                    }
                }
                
                uomMap = new HashMap<String, Object>();
                uomMap.put("uomname", StringUtil.DecodeText(jobj.optString("uomname")) );
                uomMap.put("uomtype",StringUtil.DecodeText(jobj.optString("uomtype")) );  
                uomMap.put("precision", jobj.getInt("precision"));
                uomMap.put("companyid", companyid);
                
                KwlReturnObject uomResult = accUomObj.getUnitOfMeasure(uomMap);
                int nocount = uomResult.getRecordTotalCount();
                if (nocount > 0) { // if same uom name exists in system show alert
                    throw new AccountingException(messageSource.getMessage("acc.masterConfig.uom", null, RequestContextUtils.getLocale(request)) + " '" +
       StringUtil.DecodeText(jobj.optString("uomname")) + messageSource.getMessage("acc.field.alreadyexists.", null, RequestContextUtils.getLocale(request)));
                }

                if (StringUtil.isNullOrEmpty(StringUtil.DecodeText(jobj.optString("uomid")))){
                    auditMsg = "added";
                    auditID = AuditAction.UNIT_OF_MEASURE_CREATED;
                    uomresult = accUomObj.addUoM(uomMap);
                } else {
                    auditMsg = "updated";
                    auditID = AuditAction.UNIT_OF_MEASURE_UPDATED;
                    uomMap.put("uomid", jobj.getString("uomid"));
                    uomresult = accUomObj.updateUoM(uomMap);
                }
                uom = (UnitOfMeasure) uomresult.getEntityList().get(0);
                auditTrailObj.insertAuditLog(auditID, "User " + sessionHandlerImpl.getUserFullName(request) + " has " + auditMsg + " unit of measure " + uom.getName(), request, uom.getID());
            }
        } catch (JSONException ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        } catch (AccountingException ex) {
            throw ex;
        } catch (Exception ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
        return msg;
    }
    public ModelAndView getUOMType(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        try {
            HashMap<String, Object> requestParams = AccountingManager.getGlobalParams(request);
            String stockuomid=request.getParameter("stockuomid");
            String start = request.getParameter(Constants.start);
            String limit = request.getParameter(Constants.limit);
            if(!StringUtil.isNullOrEmpty(stockuomid)){
                requestParams.put("stockuomid", stockuomid);
            }
            boolean doNotShowNAUomName = !StringUtil.isNullOrEmpty(request.getParameter("doNotShowNAUomName")) ? Boolean.parseBoolean(request.getParameter("doNotShowNAUomName")) : false;
            if (doNotShowNAUomName) {
                requestParams.put("doNotShowNAUomName", doNotShowNAUomName);
            }
            KwlReturnObject result = accUomObj.getUOMType(requestParams);
            List list = result.getEntityList();
//            int count = result.getRecordTotalCount();

            JSONArray DataJArr = getUOMTypeJson(request, list);
            /*
             * Sort on UOMSchematype SDP-13505
             */
            DataJArr = StringUtil.sortJsonArray(DataJArr, "uomschematype", false, true);

            JSONArray pagedJson = DataJArr;
            if (!StringUtil.isNullOrEmpty(start) && !StringUtil.isNullOrEmpty(limit)) {
                pagedJson = StringUtil.getPagedJSON(pagedJson, Integer.parseInt(start), Integer.parseInt(limit));
            }
            jobj.put("data", pagedJson);
            jobj.put("count", DataJArr.length());
            issuccess = true;
        } catch (SessionExpiredException ex) {
            msg = ex.getMessage();
            Logger.getLogger(accUomController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ServiceException ex) {
            msg = ex.getMessage();
            Logger.getLogger(accUomController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            msg = "" + ex.getMessage();
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accUomController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public JSONArray getUOMTypeJson(HttpServletRequest request, List list) throws ServiceException {
        JSONArray jArr = new JSONArray();
        try {
            Iterator itr = list.iterator();
            while (itr.hasNext()) {
                UOMschemaType  uomSchemaType = (UOMschemaType) itr.next();
                JSONObject obj = new JSONObject();
                obj.put("rowid", uomSchemaType.getID());
                obj.put("uomschematypeid", uomSchemaType.getID());
                obj.put("uomschematype", uomSchemaType.getName());
                obj.put("uomid", uomSchemaType.getStockuom().getID());
                obj.put("uomname", uomSchemaType.getStockuom().getName());
//                obj.put("precision", uom.getAllowedPrecision());
//                obj.put("uomtype", uom.getType());
                jArr.put(obj);
            }
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("getUoMJson : " + ex.getMessage(), ex);
        }
        return jArr;
    }
    public ModelAndView DeleteUOMSchemaType(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        try {
//            HashMap<String, Object> requestParams = AccountingManager.getGlobalParams(request);
            String uomTypeIds=request.getParameter("ids");
            String companyid = sessionHandlerImpl.getCompanyid(request);
            String uomName = "";
            if (!StringUtil.isNullOrEmpty(request.getParameter("name"))) {
                uomName = request.getParameter("name");
            }
            KwlReturnObject result = accUomObj.getProductLinkedWithUOMType(uomTypeIds, companyid); //Is used in Purchase Order ?
            List list1 = result.getEntityList();
            int count1 = list1.size();
            if (count1 > 0) {
                        throw new AccountingException("Selected record is currently used in the Product(s). So it cannot be deleted.");
            }
            accUomObj.deleteUOMSchemaForSchemaType(uomTypeIds, companyid);
            accUomObj.deleteUOMSchemaType(uomTypeIds, companyid);
//            KwlReturnObject result = accUomObj.getUOMType(requestParams);
//            List list = result.getEntityList();
//            int count = result.getRecordTotalCount();

//            JSONArray DataJArr = getUOMTypeJson(request, list);
//            jobj.put("data", DataJArr);
//            jobj.put("count", count);
            msg = messageSource.getMessage("acc.prod.shcema.del", null, RequestContextUtils.getLocale(request));
            issuccess = true;
            auditTrailObj.insertAuditLog(AuditAction.UOMSCHEMA_DELETE, "User " + sessionHandlerImpl.getUserFullName(request) + " has deleted UOMSchema : " + uomName, request, uomTypeIds);
        } catch (SessionExpiredException | ServiceException ex) {
            msg = ex.getMessage();
            Logger.getLogger(accUomController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (AccountingException | NoSuchMessageException ex) {
            msg = "" + ex.getMessage();
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
                jobj.put("isused",true);
            } catch (JSONException ex) {
                Logger.getLogger(accUomController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    public ModelAndView getUOMSchema(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        try {
            HashMap<String, Object> requestParams = AccountingManager.getGlobalParams(request);
            String uomnature=request.getParameter("uomnature");
            if (!StringUtil.isNullOrEmpty(uomnature) && uomnature.equals("Purchase")) {
                requestParams.put("uomnature", UOMNature.Purchase);
            } else if (!StringUtil.isNullOrEmpty(uomnature) && uomnature.equals("Sales")) {
                requestParams.put("uomnature", UOMNature.Sales);
            } else if (!StringUtil.isNullOrEmpty(uomnature) && uomnature.equals("Stock")) {
                requestParams.put("uomnature", UOMNature.Stock);
            } else if (!StringUtil.isNullOrEmpty(uomnature) && uomnature.equals("Transfer")) {
                requestParams.put("uomnature", UOMNature.Transfer);
            }
//            requestParams.put("uomnature",UOMNature.Purchase);
            requestParams.put("stockuomid",request.getParameter("stockuomid"));
            requestParams.put("uomschematypeid",request.getParameter("uomschematypeid"));

            KwlReturnObject result = accUomObj.getUOMSchema(requestParams);
            List list = result.getEntityList();
            int count = result.getRecordTotalCount();
            JSONArray DataJArr=new JSONArray();
            if(!StringUtil.isNullOrEmpty(uomnature) && uomnature.equals("Purchase")){
                DataJArr = accUomService.getPurchaseUOMSchemaJson(requestParams, list);
            }else if(!StringUtil.isNullOrEmpty(uomnature) && uomnature.equals("Sales")){
                DataJArr = accUomService.getSalesUOMSchemaJson(requestParams, list);
            }else if(!StringUtil.isNullOrEmpty(uomnature) && uomnature.equals("Stock")){
                 DataJArr = accUomService.getOrderUOMSchemaJson(requestParams, list);
            }else if(!StringUtil.isNullOrEmpty(uomnature) && uomnature.equals("Transfer")){
                DataJArr = accUomService.getTransferUOMSchemaJson(requestParams, list);
            }
             
            jobj.put("data", DataJArr);
            jobj.put("count", count);
            issuccess = true;
        } catch (SessionExpiredException ex) {
            msg = ex.getMessage();
            Logger.getLogger(accUomController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ServiceException ex) {
            msg = ex.getMessage();
            Logger.getLogger(accUomController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            msg = "" + ex.getMessage();
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accUomController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

//    public ModelAndView getSalesUOMSchema(HttpServletRequest request, HttpServletResponse response) {
//        JSONObject jobj = new JSONObject();
//        boolean issuccess = false;
//        String msg = "";
//        try {
//            HashMap<String, Object> requestParams = AccountingManager.getGlobalParams(request);
//
//            KwlReturnObject result = accUomObj.getUnitOfMeasure(requestParams);
//            List list = result.getEntityList();
//            int count = result.getRecordTotalCount();
//
//            JSONArray DataJArr = getSalesUOMSchemaJson(request, list);
//            jobj.put("data", DataJArr);
//            jobj.put("count", count);
//            issuccess = true;
//        } catch (SessionExpiredException ex) {
//            msg = ex.getMessage();
//            Logger.getLogger(accUomController.class.getName()).log(Level.SEVERE, null, ex);
//        } catch (ServiceException ex) {
//            msg = ex.getMessage();
//            Logger.getLogger(accUomController.class.getName()).log(Level.SEVERE, null, ex);
//        } catch (Exception ex) {
//            msg = "" + ex.getMessage();
//        } finally {
//            try {
//                jobj.put("success", issuccess);
//                jobj.put("msg", msg);
//            } catch (JSONException ex) {
//                Logger.getLogger(accUomController.class.getName()).log(Level.SEVERE, null, ex);
//            }
//        }
//        return new ModelAndView("jsonView", "model", jobj.toString());
//    }
//
    
    public ModelAndView saveUOMSchemaType(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;

        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("UoM_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);

        TransactionStatus status = txnManager.getTransaction(def);
        try {
            boolean isEdit = Boolean.parseBoolean(request.getParameter("isEdit"));
            String action = isEdit ? "updated":"added";
            saveUOMSchemaType(request);
            issuccess = true;
            msg = messageSource.getMessage("acc.uom.uomschema", new Object[]{action}, RequestContextUtils.getLocale(request));   //"Unit of measure has been updated successfully";
            txnManager.commit(status);
        } catch (SessionExpiredException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            Logger.getLogger(accUomController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = "" + ex.getMessage();
            Logger.getLogger(accUomController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accUomController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    
     public ModelAndView getISUOMSchemaConfiguredandUsed(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        try {
            Map<String, Object> requestData = new HashMap<>();
            requestData.put(Constants.companyKey, AccountingManager.getCompanyidFromRequest(request));
            String stockUomid=request.getParameter("oldStockUomid");
            requestData.put("stockuomid", stockUomid);
            requestData.put("uomschematype", request.getParameter("uomschematype"));
            JSONObject resultObj = accUomService.getISUOMSchemaConfiguredandUsed(requestData);
            String newStockUomid=request.getParameter("newStockUomid");
            int count = resultObj.has("count") ? (Integer) resultObj.get("count") : 0;
            jobj.put("count", count);
            jobj.put("newStockUomid", newStockUomid);
            jobj.put("oldStockUomid", stockUomid);
            issuccess = true;
        } catch (SessionExpiredException | ServiceException | JSONException ex) {
            msg = "" + ex.getMessage();
            Logger.getLogger(accUomController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accUomController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public void saveUOMSchemaType(HttpServletRequest request) throws ServiceException, AccountingException, SessionExpiredException {
        try {
            String companyid = sessionHandlerImpl.getCompanyid(request);
            boolean isEdit = Boolean.parseBoolean(request.getParameter("isEdit"));
            HashMap<String, Object> uomMap;
            uomMap = new HashMap<String, Object>();
            uomMap.put("schemaName",StringUtil.DecodeText(request.getParameter("schemaName")) );
            uomMap.put("stockuomid", StringUtil.DecodeText(request.getParameter("stockuomid")));
            uomMap.put("rowid", request.getParameter("rowid"));
            uomMap.put("companyid", companyid);
            KwlReturnObject  uomresult = accUomObj.searchUoMSchemaType(StringUtil.DecodeText(request.getParameter("schemaName")),companyid);
            int nocount = uomresult.getRecordTotalCount();
            if (nocount > 0 && !isEdit) { // if same uom schema name exists in system show alert
                throw new AccountingException(messageSource.getMessage("acc.accPref.inventoryschema", null, RequestContextUtils.getLocale(request)) + " '" +StringUtil.DecodeText(request.getParameter("schemaName")) + messageSource.getMessage("acc.field.alreadyexists.", null, RequestContextUtils.getLocale(request)));
            }
            uomresult = accUomObj.addUOMSchemaType(uomMap);
            UOMschemaType uOMschemaType = (UOMschemaType) uomresult.getEntityList().get(0);
            
            String auditMsg = isEdit ? " has updated UOM Schema : " : " has added UOM Schema : ";
            auditTrailObj.insertAuditLog(AuditAction.UOMSCHEMA_ADDEDIT, "User " + sessionHandlerImpl.getUserFullName(request) + auditMsg + uOMschemaType.getName(), request, uOMschemaType.getID());
        } catch (SessionExpiredException | ServiceException ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
    }
    public ModelAndView saveUOMSchema(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "", schemaName = "";
        boolean issuccess = false;

        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("UoM_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);

        TransactionStatus status = txnManager.getTransaction(def);
        try {
            if (!StringUtil.isNullOrEmpty(request.getParameter("schemaname"))) {
                schemaName = request.getParameter("schemaname");
            }
            saveUOMSchema(request);
            issuccess = true;
            msg = messageSource.getMessage("acc.uom.update", null, RequestContextUtils.getLocale(request));   //"Unit of measure has been updated successfully";
            txnManager.commit(status);
            auditTrailObj.insertAuditLog(AuditAction.CONFIGURE_UOMSCHEMA, "User " + sessionHandlerImpl.getUserFullName(request) + " has configured Uom Schema : " + schemaName, request, "258");
        } catch (SessionExpiredException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            Logger.getLogger(accUomController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = "" + ex.getMessage();
            Logger.getLogger(accUomController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accUomController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public void saveUOMSchema(HttpServletRequest request) throws ServiceException, AccountingException, SessionExpiredException {
        try {
            String  uomschematypeid = request.getParameter("uomschematypeid");
            String companyid = sessionHandlerImpl.getCompanyid(request);

            if(!StringUtil.isNullOrEmpty(uomschematypeid)){
                accUomObj.deleteUOMSchemaForSchemaType(uomschematypeid, companyid);
            }
            KwlReturnObject uomresult;
            HashMap<String, Object> uomMap;
            JSONArray jArr = new JSONArray(request.getParameter("purchasedetail"));
            for (int i = 0; i < jArr.length(); i++) {
                JSONObject jobj = jArr.getJSONObject(i);
                uomMap = new HashMap<String, Object>();
                uomMap.put("rowid", jobj.getString("rowid"));
                uomMap.put("purchaseuom", jobj.getString("purchaseuom"));
                uomMap.put("baseuom", jobj.getString("baseuom"));
                uomMap.put("baseuomrate", jobj.getDouble("baseuomrate"));
                uomMap.put("rateperuom", jobj.getDouble("rateperuom"));
                uomMap.put("uomschematypeid", uomschematypeid);
                uomMap.put("uomnature", UOMNature.Purchase);
                uomMap.put("companyid", companyid);
                uomresult = accUomObj.addUOMSchema(uomMap);
            }
            
            jArr = new JSONArray(request.getParameter("salesdetail"));
            for (int i = 0; i < jArr.length(); i++) {
                JSONObject jobj = jArr.getJSONObject(i);
                uomMap = new HashMap<String, Object>();
                uomMap.put("rowid", jobj.getString("rowid"));
                uomMap.put("salesuom", jobj.getString("salesuom"));
                uomMap.put("baseuom", jobj.getString("baseuom"));
                uomMap.put("baseuomrate", jobj.getDouble("baseuomrate"));
                uomMap.put("rateperuom", jobj.getDouble("rateperuom"));
                uomMap.put("uomschematypeid", uomschematypeid);
                uomMap.put("uomnature", UOMNature.Sales);
                uomMap.put("companyid", companyid);
                uomresult = accUomObj.addUOMSchema(uomMap);
            }       
            if (!StringUtil.isNullOrEmpty(request.getParameter("stockdetail"))) {
                jArr = new JSONArray(request.getParameter("stockdetail"));
                for (int i = 0; i < jArr.length(); i++) {
                    JSONObject jobj = jArr.getJSONObject(i);
                    uomMap = new HashMap<String, Object>();
                    uomMap.put("rowid", jobj.getString("rowid"));
                    uomMap.put("orderuom", jobj.getString("orderuom"));
                    uomMap.put("baseuom", jobj.getString("baseuom"));
                    uomMap.put("baseuomrate", jobj.getDouble("baseuomrate"));
                    uomMap.put("rateperuom", jobj.getDouble("rateperuom"));
                    uomMap.put("uomschematypeid", uomschematypeid);
                    uomMap.put("uomnature", UOMNature.Stock);
                    uomMap.put("companyid", companyid);
                    uomresult = accUomObj.addUOMSchema(uomMap);
                }
            }
            if (!StringUtil.isNullOrEmpty(request.getParameter("transferdetail"))) {
                jArr = new JSONArray(request.getParameter("transferdetail"));
                for (int i = 0; i < jArr.length(); i++) {
                    JSONObject jobj = jArr.getJSONObject(i);
                    uomMap = new HashMap<String, Object>();
                    uomMap.put("rowid", jobj.getString("rowid"));
                    uomMap.put("transferuom", jobj.getString("transferuom"));
                    uomMap.put("baseuom", jobj.getString("baseuom"));
                    uomMap.put("baseuomrate", jobj.getDouble("baseuomrate"));
                    uomMap.put("rateperuom", jobj.getDouble("rateperuom"));
                    uomMap.put("uomschematypeid", uomschematypeid);
                    uomMap.put("uomnature", UOMNature.Transfer);
                    uomMap.put("companyid", companyid);
                    uomresult = accUomObj.addUOMSchema(uomMap);
                }
            }

        } catch (Exception ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
    }
    
    public ModelAndView importUnitOfMeasure(HttpServletRequest request, HttpServletResponse response) throws ServiceException {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        try {
            String eParams = request.getParameter("extraParams");
            JSONObject extraParams = StringUtil.isNullOrEmpty(eParams) ? new JSONObject() : new JSONObject(eParams);
            extraParams.put("Company", sessionHandlerImpl.getCompanyid(request));

            String doAction = request.getParameter("do");
            HashMap<String, Object> requestParams = importHandler.getImportRequestParams(request);
            requestParams.put("extraParams", extraParams);
            requestParams.put("extraObj", null);
            requestParams.put("servletContext", this.getServletContext());

            if (doAction.compareToIgnoreCase("import") == 0) {
                System.out.println("A(( Import start : " + new Date());
                JSONObject datajobj = new JSONObject();
                JSONObject resjson = new JSONObject(request.getParameter("resjson").toString());
                JSONArray resjsonJArray = resjson.getJSONArray("root");

                String filename = request.getParameter("filename");
                datajobj.put("filename", filename);

                String destinationDirectory = storageHandlerImpl.GetDocStorePath() + "importplans";
                File filepath = new File(destinationDirectory + "/" + filename);
                datajobj.put("FilePath", filepath);

                datajobj.put("resjson", resjsonJArray);

                jobj = importUnitOfMeasureRecords(request, datajobj);
                System.out.println("A(( Import end : " + new Date());
            } else if (doAction.compareToIgnoreCase("validateData") == 0) {
                System.out.println("A(( Validation start : " + new Date());
                jobj = importHandler.validateFileData(requestParams);
                System.out.println("A(( Validation end : " + new Date());
            }
            issuccess = true;
        } catch (Exception ex) {
            issuccess = false;
            msg = "" + ex.getMessage();

            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException e) {
                Logger.getLogger(accUomController.class.getName()).log(Level.SEVERE, e.getMessage(), e);
            }

            Logger.getLogger(accUomController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public JSONObject importUnitOfMeasureRecords(HttpServletRequest request, JSONObject jobj) throws AccountingException, IOException, SessionExpiredException, JSONException {
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("import_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        boolean commitedEx = false;
        boolean issuccess = true;
        String msg = "";
        FileInputStream fileInputStream = null;
        BufferedReader br = null;
        int total = 0, failed = 0;
        String companyid = sessionHandlerImpl.getCompanyid(request);
        String userId = sessionHandlerImpl.getUserid(request);
        String fileName = jobj.getString("filename");
        String masterPreference = request.getParameter("masterPreference");

        JSONObject returnObj = new JSONObject();

        try {
            fileInputStream = new FileInputStream(jobj.getString("FilePath"));
            br = new BufferedReader(new InputStreamReader(fileInputStream));
            String record = "";
            int cnt = 0;

            StringBuilder failedRecords = new StringBuilder();

            HashMap<String, Integer> columnConfig = new HashMap<String, Integer>();
            JSONArray jSONArray = jobj.getJSONArray("resjson");
            List headArrayList = new ArrayList();
            for (int i = 0; i < jSONArray.length(); i++) {
                JSONObject jSONObject = jSONArray.getJSONObject(i);
                headArrayList.add(jSONObject.get("csvheader"));

                columnConfig.put(jSONObject.getString("dataindex"), jSONObject.getInt("csvindex"));
            }

            failedRecords.append(createCSVrecord(headArrayList.toArray()) + "\"Error Message\""); // failedRecords.append("\"Row No.\","+createCSVrecord(fileData)+"\"Error Message\"");

            while ((record = br.readLine()) != null) {
                if (cnt != 0) {
                    String[] recarr = record.split(",");
                    try {

                        String uomName = "";
                        if (columnConfig.containsKey("uomname")) {
                            uomName = recarr[(Integer) columnConfig.get("uomname")].replaceAll("\"", "").trim();
                            if (!StringUtil.isNullOrEmpty(uomName)) {
                                uomName = uomName.replaceAll("\"", "");
                            } else {
                                throw new AccountingException("Name is not available.");
                            }
                        } else {
                            throw new AccountingException("Name column is not found.");
                        }
                        
                        String uomType = "";
                        if (columnConfig.containsKey("uomtype")) {
                            uomType = recarr[(Integer) columnConfig.get("uomtype")].replaceAll("\"", "").trim();
                            if (!StringUtil.isNullOrEmpty(uomType)) {
                                uomType = uomType.replaceAll("\"", "");
                            } else {
                                throw new AccountingException("Type is not available.");
                            }
                        } else {
                            throw new AccountingException("Type column is not found.");
                        }
                        
                        String allowedPrecision = "";
                        if (columnConfig.containsKey("precision")) {
                            allowedPrecision = recarr[(Integer) columnConfig.get("precision")].replaceAll("\"", "").trim();
                            if (!StringUtil.isNullOrEmpty(allowedPrecision)) {
                                allowedPrecision = allowedPrecision.replaceAll("\"", "");
                            } else {
                                throw new AccountingException("Allowed Precision is not available.");
                            }
                        } else {
                            throw new AccountingException("Allowed Precision column is not found.");
                        }

                        // getting unit of measure object
                        HashMap<String, Object> filterParams = new HashMap<String, Object>();
                        filterParams.put("uomname", uomName);
                        filterParams.put("companyid", companyid);
                        KwlReturnObject result = accUomObj.getUnitOfMeasure(filterParams);
                        int nocount = result.getRecordTotalCount();
                        if (nocount > 0) { // if same uom name exists in system exclude record
                            throw new AccountingException("Unit of Measure '" + uomName + "' already exists.");
                        }
                        
                        // for saving unit of measure
                        HashMap<String, Object> uomMap = new HashMap<String, Object>();
                        uomMap.put("uomname", uomName);
                        uomMap.put("uomtype", uomType);
                        uomMap.put("precision", Integer.parseInt(allowedPrecision));
                        uomMap.put("companyid", companyid);
                        
                        accUomObj.addUoM(uomMap);

                    } catch (Exception ex) {
                        failed++;
                        String errorMsg = ex.getMessage(), invalidColumns = "";
                        try {
                            JSONObject errorLog = new JSONObject(errorMsg);
                            errorMsg = errorLog.getString("errorMsg");
                            invalidColumns = errorLog.getString("invalidColumns");
                        } catch (JSONException jex) { }
                        
                        failedRecords.append("\n" + createCSVrecord(recarr) + "\"" + errorMsg.replaceAll("\"", "") + "\"");
                    }
                    total++;
                }
                cnt++;
            }

            if (failed > 0) {
                createFailureFiles(fileName, failedRecords, ".csv");
            }

            int success = total - failed;
            if (total == 0) {
                msg = messageSource.getMessage("acc.field.Emptyfile", null, RequestContextUtils.getLocale(request));
            } else if (success == 0) {
//                issuccess = false;
                msg = messageSource.getMessage("acc.rem.169", null, RequestContextUtils.getLocale(request));
            } else if (success == total) {
                msg = messageSource.getMessage("acc.rem.168", null, RequestContextUtils.getLocale(request));
            } else {
                msg = messageSource.getMessage("acc.field.Imported", null, RequestContextUtils.getLocale(request))+ " " + success + " "+ messageSource.getMessage("acc.field.record", null, RequestContextUtils.getLocale(request)) + (success > 1 ? "s" : "") + messageSource.getMessage("acc.field.successfully.", null, RequestContextUtils.getLocale(request));
                msg += (failed == 0 ? "." : " "+messageSource.getMessage("acc.field.andfailedtoimport", null, RequestContextUtils.getLocale(request))+ " " + failed + " "+ messageSource.getMessage("acc.field.record", null, RequestContextUtils.getLocale(request)) + (failed > 1 ? "s" : "") + ".");
            }

            try {
                txnManager.commit(status);
            } catch (Exception ex) {
                commitedEx = true;
                throw ex;
            }
        } catch (Exception ex) {
            if (!commitedEx) { //if exception occurs during commit then dont call rollback
                txnManager.rollback(status);
            }
            issuccess = false;
            msg = "" + ex.getMessage();

            Logger.getLogger(accUomController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            throw new AccountingException("Error While Importing Records.");
        } finally {
            fileInputStream.close();
            br.close();

            DefaultTransactionDefinition ldef = new DefaultTransactionDefinition();
            ldef.setName("import_Tx");
            ldef.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
            TransactionStatus lstatus = txnManager.getTransaction(ldef);

            try {
                //Insert Integration log
                HashMap<String, Object> logDataMap = new HashMap<String, Object>();
                logDataMap.put("FileName", getActualFileName(fileName));
                logDataMap.put("StorageName", fileName);
                logDataMap.put("Log", msg);
                logDataMap.put("Type", "csv");
                logDataMap.put("FailureFileType", failed>0?"csv":"");
                logDataMap.put("TotalRecs", total);
                logDataMap.put("Rejected", failed);
                logDataMap.put("Module", Constants.Acc_Unit_Of_Measure_ModuleId);
                logDataMap.put("ImportDate", new Date());
                logDataMap.put("User", userId);
                logDataMap.put("Company", companyid);
                importDao.saveImportLog(logDataMap);

                String tableName = importDao.getTableName(fileName);
                importDao.removeFileTable(tableName); // Remove table after importing all records

                txnManager.commit(lstatus);
            } catch (Exception ex) {
                txnManager.rollback(lstatus);
                Logger.getLogger(accUomController.class.getName()).log(Level.SEVERE, null, ex);
            }

            try {
                returnObj.put("success", issuccess);
                returnObj.put("msg", msg);
                returnObj.put("totalrecords", total);
                returnObj.put("successrecords", total - failed);
                returnObj.put("failedrecords", failed);
                returnObj.put("Module",Constants.Acc_Product_Master_ModuleId);
                returnObj.put("filename", getActualFileName(fileName));
            } catch (JSONException ex) {
                Logger.getLogger(accUomController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        return returnObj;
    }
    
    public static String getActualFileName(String storageName) {
        String ext = storageName.substring(storageName.lastIndexOf("."));
        String actualName = storageName.substring(0, storageName.lastIndexOf("_"));
        actualName = actualName + ext;
        return actualName;
    }
    
    public String createCSVrecord(Object[] listArray) {
        String rec = "";
        for (int i = 0; i < listArray.length; i++) {    // Discard columns id at index 0 and isvalid, invalidColumns, validationlog at last 3 indexes.
//            String s = (listArray[i]==null)?"":listArray[i].toString();
            rec += "\"" + (listArray[i] == null ? "" : listArray[i].toString().replaceAll("\"", "")) + "\",";
        }
        return rec;
    }
    
    public void createFailureFiles(String filename, StringBuilder failedRecords, String ext) {
        String destinationDirectory;
        try {
            destinationDirectory = storageHandlerImpl.GetDocStorePath() + "importplans";
            if (StringUtil.isNullOrEmpty(ext)) {
                ext = filename.substring(filename.lastIndexOf("."));
            }
            filename = filename.substring(0, filename.lastIndexOf("."));

            java.io.FileOutputStream failurefileOut = new java.io.FileOutputStream(destinationDirectory + "/" + filename + ImportLog.failureTag + ext);
            failurefileOut.write(failedRecords.toString().getBytes());
            failurefileOut.flush();
            failurefileOut.close();
        } catch (Exception ex) {
            System.out.println("\nError file write [success/failed] " + ex);
        }
    }
    
    public ModelAndView importUOMSchemaType(HttpServletRequest request, HttpServletResponse response) throws ServiceException {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        try {
            String eParams = request.getParameter("extraParams");
            JSONObject extraParams = StringUtil.isNullOrEmpty(eParams) ? new JSONObject() : new JSONObject(eParams);
            extraParams.put("Company", sessionHandlerImpl.getCompanyid(request));
            
            String doAction = request.getParameter("do");
            HashMap<String, Object> requestParams = importHandler.getImportRequestParams(request);
            requestParams.put("extraParams", extraParams);
            requestParams.put("extraObj", null);
            requestParams.put("servletContext", this.getServletContext());
            requestParams.put("createUomSchemaTypeFlag", request.getParameter("createUomSchemaTypeFlag"));

            if (doAction.compareToIgnoreCase("import") == 0) {
                System.out.println("A(( Import start : " + new Date());
                JSONObject datajobj = new JSONObject();
                JSONObject resjson = new JSONObject(request.getParameter("resjson").toString());
                JSONArray resjsonJArray = resjson.getJSONArray("root");

                String filename = request.getParameter("filename");
                datajobj.put("filename", filename);

                String destinationDirectory = storageHandlerImpl.GetDocStorePath() + "importplans";
                File filepath = new File(destinationDirectory + "/" + filename);
                datajobj.put("FilePath", filepath);

                datajobj.put("resjson", resjsonJArray);

                jobj = importUOMSchemaTypeRecords(request, datajobj);
                System.out.println("A(( Import end : " + new Date());
            } else if (doAction.compareToIgnoreCase("validateData") == 0) {
                System.out.println("A(( Validation start : " + new Date());
                jobj = importHandler.validateFileData(requestParams);
                System.out.println("A(( Validation end : " + new Date());
            }
            issuccess = true;
        } catch (Exception ex) {
            issuccess = false;
            msg = "" + ex.getMessage();

            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException e) {
                Logger.getLogger(accUomController.class.getName()).log(Level.SEVERE, e.getMessage(), e);
            }

            Logger.getLogger(accUomController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public JSONObject importUOMSchemaTypeRecords(HttpServletRequest request, JSONObject jobj) throws AccountingException, IOException, SessionExpiredException, JSONException {
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("import_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        boolean commitedEx = false;
        boolean issuccess = true;
        String msg = "";
        FileInputStream fileInputStream = null;
        BufferedReader br = null;
        int total = 0, failed = 0;
        String companyid = sessionHandlerImpl.getCompanyid(request);
        String userId = sessionHandlerImpl.getUserid(request);
        String fileName = jobj.getString("filename");
        String masterPreference = request.getParameter("masterPreference");
        String prevUomSchema = "";
        JSONArray jArr = new JSONArray();
        boolean isRecordFailed = false;

        JSONObject returnObj = new JSONObject();

        try {
            boolean createUomSchemaTypeFlag = (request.getParameter("createUomSchemaTypeFlag") != null) ? Boolean.parseBoolean(request.getParameter("createUomSchemaTypeFlag")) : false;
            boolean deleteExistingUomSchemaFlag = (request.getParameter("deleteExistingUomSchemaFlag") != null) ? Boolean.parseBoolean(request.getParameter("deleteExistingUomSchemaFlag")) : false;
            
            fileInputStream = new FileInputStream(jobj.getString("FilePath"));
            br = new BufferedReader(new InputStreamReader(fileInputStream));
            String record = "";
            int cnt = 0;

            StringBuilder failedRecords = new StringBuilder();
            HashMap<String, Integer> columnConfig = new HashMap<String, Integer>();
            JSONArray jSONArray = jobj.getJSONArray("resjson");
            
            for (int i = 0; i < jSONArray.length(); i++) {
                JSONObject jSONObject = jSONArray.getJSONObject(i);
                columnConfig.put(jSONObject.getString("dataindex"), jSONObject.getInt("csvindex"));
            }

            while ((record = br.readLine()) != null) {
                String[] recarr = record.split(",");
                if (cnt == 0) {
                    failedRecords.append(createCSVrecord(recarr) + "\"Error Message\"");
                }
                
                if (cnt != 0) {
                    try {
                        UnitOfMeasure stockUom = null;
                        if (columnConfig.containsKey("stockUomName")) {
                            String stockUomName = recarr[(Integer) columnConfig.get("stockUomName")].replaceAll("\"", "").trim();
                            if (!StringUtil.isNullOrEmpty(stockUomName)) {
                                stockUom = getUOMByName(stockUomName, companyid);
                                if (stockUom == null) {
                                        throw new AccountingException("Stock UOM is not found for " + stockUomName);
                                    }
                            } else {
                                throw new AccountingException("Stock UOM is not available.");
                            }
                        } else {
                            throw new AccountingException("Stock UOM column is not found.");
                        }
                        
                        UOMschemaType uomSchemaType = null;
                        if (columnConfig.containsKey("uomSchemaTypeName")) {
                            String uomSchemaTypeName = recarr[(Integer) columnConfig.get("uomSchemaTypeName")].replaceAll("\"", "").trim();
                            if (!StringUtil.isNullOrEmpty(uomSchemaTypeName)) {
                                uomSchemaType = getUOMschemaTypeByName(uomSchemaTypeName, companyid);
                                if ((uomSchemaType == null && createUomSchemaTypeFlag)||(masterPreference.equalsIgnoreCase("2")&&uomSchemaType==null)) {
                                    HashMap<String, Object> uomMap;
                                    uomMap = new HashMap<String, Object>();
                                    uomMap.put("schemaName", uomSchemaTypeName);
                                    uomMap.put("stockuomid", stockUom.getID());
                                    uomMap.put("companyid", companyid);
                                    KwlReturnObject uomSchemaResult = accUomObj.addUOMSchemaType(uomMap);
                                    uomSchemaType = (UOMschemaType) uomSchemaResult.getEntityList().get(0);
                                } else if (uomSchemaType == null && !createUomSchemaTypeFlag) {
                                    throw new AccountingException("Schema Name is not found for " + uomSchemaTypeName);
                                }
                            } else {
                                throw new AccountingException("Schema Name is not available.");
                            }
                        } else {
                            throw new AccountingException("Schema Name column is not found.");
                        }

                        String schemaType = "";
                        if (columnConfig.containsKey("schemaType")) {
                            schemaType = recarr[(Integer) columnConfig.get("schemaType")].replaceAll("\"", "").trim();
                            if (!StringUtil.isNullOrEmpty(schemaType)) {
                                if (schemaType.equalsIgnoreCase("Purchase")) {
                                    schemaType = "0";
                                } else if (schemaType.equalsIgnoreCase("Sales")) {
                                    schemaType = "1";
                                } else if (schemaType.equalsIgnoreCase("Transfer")) {
                                    schemaType = "3";
                                } else if (schemaType.equalsIgnoreCase("Order")) {
                                    schemaType = "2";
                                } else {
                                    throw new AccountingException("Value is not valid for column Schema Type, It should be like \"Purchase\"/\"Sales\"/\"Order\"/\"Transfer\"");
                                }
                            } else {
                                throw new AccountingException("Schema Type is not available.");
                            }
                        } else {
                            throw new AccountingException("Schema Type column is not found.");
                        }
                        
                        UnitOfMeasure uom = null;
                        if (columnConfig.containsKey("uomName")) {
                            String uomName = recarr[(Integer) columnConfig.get("uomName")].replaceAll("\"", "").trim();
                            if (!StringUtil.isNullOrEmpty(uomName)) {
                                uom = getUOMByName(uomName, companyid);
                                if (uom == null) {
                                    if (masterPreference.equalsIgnoreCase("2")) {
                                        HashMap<String, Object> newUomMap1 = new HashMap<String, Object>();
                                        newUomMap1.put("uomname",StringUtil.DecodeText(uomName) );
                                        newUomMap1.put("uomtype",StringUtil.DecodeText(uomName) );
                                        newUomMap1.put("precision", 0);
                                        newUomMap1.put("companyid", companyid);

                                        KwlReturnObject uomresult = accUomObj.addUoM(newUomMap1);
                                        uom = (UnitOfMeasure) uomresult.getEntityList().get(0);
                                     String   productUOMID = uom.getID();
                                    } else {
                                        throw new AccountingException("UOM is not found for " + uomName);
                                    }
                                }
                            } else {
                                throw new AccountingException("UOM is not available.");
                            }
                        } else {
                            throw new AccountingException("UOM column is not found.");
                        }
                        
                        String quantity = "";
                        if (columnConfig.containsKey("baseuomrate")) {
                            quantity = recarr[(Integer) columnConfig.get("baseuomrate")].replaceAll("\"", "").trim();
                            if (StringUtil.isNullOrEmpty(quantity)) {
                                throw new AccountingException("Quantity is not available.");
                            }
                        } else {
                            throw new AccountingException("Quantity column is not found.");
                        }
                        
                        if (deleteExistingUomSchemaFlag) {
                            accUomObj.deleteUOMSchemaForSchemaType(uomSchemaType.getID(), companyid);
                        }

                        if (!prevUomSchema.equalsIgnoreCase(uomSchemaType.getName())) {
                            prevUomSchema = uomSchemaType.getName();
                            
                            if (jArr.length() > 0 && !isRecordFailed) {
                                for (int i = 0; i < jArr.length(); i++) {
                                    JSONObject uomJobj = jArr.getJSONObject(i);
                                    HashMap<String, Object> uomMap = new HashMap<String, Object>();
                                    
                                    // for updating existing configured uom schema
                                    if (!deleteExistingUomSchemaFlag) {
                                        HashMap<String, Object> requestParams = new HashMap<String, Object>();
                                        requestParams.put("companyid", uomJobj.get("companyid"));
                                        if (uomJobj.get("uomnature").equals(UOMNature.Purchase)) {
                                            requestParams.put("uomnature", UOMNature.Purchase);
                                            requestParams.put("purchaseuom", uomJobj.get("purchaseuom"));
                                        } else if(uomJobj.get("uomnature").equals(UOMNature.Sales)){
                                            requestParams.put("uomnature", UOMNature.Sales);
                                            requestParams.put("salesuom", uomJobj.get("salesuom"));
                                        }else if(uomJobj.get("uomnature").equals(UOMNature.Stock)){
                                            requestParams.put("uomnature", UOMNature.Stock);
                                            requestParams.put("orderuom", uomJobj.get("orderuom"));
                                        }else if(uomJobj.get("uomnature").equals(UOMNature.Transfer)){
                                            requestParams.put("uomnature", UOMNature.Transfer);
                                            requestParams.put("transferuom", uomJobj.get("transferuom"));
                                        }
                                        requestParams.put("uomschematypeid", uomJobj.get("uomschematypeid"));
                                        requestParams.put("stockuomid", uomJobj.get("baseuom"));
                                        
                                        KwlReturnObject uomSchemaResult = accUomObj.getUOMSchema(requestParams);
                                        
                                        if (uomSchemaResult != null && !uomSchemaResult.getEntityList().isEmpty()) {
                                            UOMSchema uomSchema = (UOMSchema) uomSchemaResult.getEntityList().get(0);
                                            uomMap.put("rowid", uomSchema.getID());
                                        }
                                    }
                                    
                                    if (uomJobj.get("uomnature").equals(UOMNature.Purchase)) {
                                        uomMap.put("purchaseuom", uomJobj.get("purchaseuom"));
                                        uomMap.put("uomnature", UOMNature.Purchase);
                                    } else if (uomJobj.get("uomnature").equals(UOMNature.Sales)) {
                                        uomMap.put("salesuom", uomJobj.get("salesuom"));
                                        uomMap.put("uomnature", UOMNature.Sales);
                                    } else if (uomJobj.get("uomnature").equals(UOMNature.Stock)) {
                                        uomMap.put("orderuom", uomJobj.get("orderuom"));
                                        uomMap.put("uomnature", UOMNature.Stock);
                                    } else if (uomJobj.get("uomnature").equals(UOMNature.Transfer)) {
                                        uomMap.put("transferuom", uomJobj.get("transferuom"));
                                        uomMap.put("uomnature", UOMNature.Transfer);
                                    }
                                    uomMap.put("baseuom", uomJobj.get("baseuom"));
                                    uomMap.put("baseuomrate", uomJobj.get("baseuomrate"));
                                    uomMap.put("rateperuom", uomJobj.get("rateperuom"));
                                    uomMap.put("uomschematypeid", uomJobj.get("uomschematypeid"));
                                    uomMap.put("companyid", uomJobj.get("companyid"));
                                    
                                    KwlReturnObject uomSchemaResult = accUomObj.addUOMSchema(uomMap);
                                }
                            }
                            
                            isRecordFailed = false;
                            jArr = new JSONArray();
                        }
                        
                        // for saving uom Schema
                        JSONObject uomJobj = new JSONObject();
                        if (schemaType.equalsIgnoreCase("0")) {
                            uomJobj.put("purchaseuom", uom.getID());
                            uomJobj.put("uomnature", UOMNature.Purchase);
                        } else if (schemaType.equalsIgnoreCase("1")) {
                            uomJobj.put("salesuom", uom.getID());
                            uomJobj.put("uomnature", UOMNature.Sales);
                        } else if (schemaType.equalsIgnoreCase("2")) {
                            uomJobj.put("orderuom", uom.getID());
                            uomJobj.put("uomnature", UOMNature.Stock);
                        } else if (schemaType.equalsIgnoreCase("3")) {
                            uomJobj.put("transferuom", uom.getID());
                            uomJobj.put("uomnature", UOMNature.Transfer);
                        }
                        uomJobj.put("baseuom", uomSchemaType.getStockuom().getID());
                        uomJobj.put("baseuomrate", Double.parseDouble(quantity));
                        uomJobj.put("rateperuom", 0.0);
                        uomJobj.put("uomschematypeid", uomSchemaType.getID());
                        uomJobj.put("companyid", companyid);
                        
                        jArr.put(uomJobj);

                    } catch (Exception ex) {
                        failed++;
                        isRecordFailed = true;
                        String errorMsg = ex.getMessage(), invalidColumns = "";
                        try {
                            JSONObject errorLog = new JSONObject(errorMsg);
                            errorMsg = errorLog.getString("errorMsg");
                            invalidColumns = errorLog.getString("invalidColumns");
                        } catch (JSONException jex) {
                        }

                        failedRecords.append("\n" + createCSVrecord(recarr) + "\"" + errorMsg.replaceAll("\"", "") + "\"");
                    }
                    total++;
                }
                cnt++;
            }

            if (jArr.length() > 0 && !isRecordFailed) {
                for (int i = 0; i < jArr.length(); i++) {
                    JSONObject uomJobj = jArr.getJSONObject(i);
                    
                    HashMap<String, Object> uomMap = new HashMap<String, Object>();
                    if (uomJobj.get("uomnature").equals(UOMNature.Purchase)) {
                        uomMap.put("purchaseuom", uomJobj.get("purchaseuom"));
                        uomMap.put("uomnature", UOMNature.Purchase);
                    } else if(uomJobj.get("uomnature").equals(UOMNature.Sales)) {
                        uomMap.put("salesuom", uomJobj.get("salesuom"));
                        uomMap.put("uomnature", UOMNature.Sales);
                    }else if(uomJobj.get("uomnature").equals(UOMNature.Stock)) {
                        uomMap.put("orderuom", uomJobj.get("orderuom"));
                        uomMap.put("uomnature", UOMNature.Stock);
                    }else if(uomJobj.get("uomnature").equals(UOMNature.Transfer)) {
                        uomMap.put("transferuom", uomJobj.get("transferuom"));
                        uomMap.put("uomnature", UOMNature.Transfer);
                    }
                    uomMap.put("baseuom", uomJobj.get("baseuom"));
                    uomMap.put("baseuomrate", uomJobj.get("baseuomrate"));
                    uomMap.put("rateperuom", uomJobj.get("rateperuom"));
                    uomMap.put("uomschematypeid", uomJobj.get("uomschematypeid"));
                    uomMap.put("companyid", uomJobj.get("companyid"));
                    
                    KwlReturnObject uomSchemaResult = accUomObj.addUOMSchema(uomMap);
                }
            }
            
            if (failed > 0) {
                createFailureFiles(fileName, failedRecords, ".csv");
            }

            int success = total - failed;
            if (total == 0) {
                msg = messageSource.getMessage("acc.field.Emptyfile", null, RequestContextUtils.getLocale(request));
            } else if (success == 0) {
//                issuccess = false;
                msg = messageSource.getMessage("acc.rem.169", null, RequestContextUtils.getLocale(request));
            } else if (success == total) {
                msg = messageSource.getMessage("acc.rem.168", null, RequestContextUtils.getLocale(request));
            } else {
                msg = messageSource.getMessage("acc.field.Imported", null, RequestContextUtils.getLocale(request))+ " " + success + " "+ messageSource.getMessage("acc.field.record", null, RequestContextUtils.getLocale(request)) + (success > 1 ? "s" : "") + messageSource.getMessage("acc.field.successfully.", null, RequestContextUtils.getLocale(request));
                msg += (failed == 0 ? "." : " "+messageSource.getMessage("acc.field.andfailedtoimport", null, RequestContextUtils.getLocale(request))+ " " + failed + " "+ messageSource.getMessage("acc.field.record", null, RequestContextUtils.getLocale(request)) + (failed > 1 ? "s" : "") + ".");
            }

            try {
                txnManager.commit(status);
            } catch (Exception ex) {
                commitedEx = true;
                throw ex;
            }
        } catch (Exception ex) {
            if (!commitedEx) { // if exception occurs during commit then dont call rollback
                txnManager.rollback(status);
            }
            issuccess = false;
            msg = "" + ex.getMessage();

            Logger.getLogger(accUomController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            throw new AccountingException("Error While Importing Records.");
        } finally {
            fileInputStream.close();
            br.close();

            DefaultTransactionDefinition ldef = new DefaultTransactionDefinition();
            ldef.setName("import_Tx");
            ldef.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
            TransactionStatus lstatus = txnManager.getTransaction(ldef);

            try {
                //Insert Integration log
                HashMap<String, Object> logDataMap = new HashMap<String, Object>();
                logDataMap.put("FileName", getActualFileName(fileName));
                logDataMap.put("StorageName", fileName);
                logDataMap.put("Log", msg);
                logDataMap.put("Type", "csv");
                logDataMap.put("FailureFileType", failed>0?"csv":"");
                logDataMap.put("TotalRecs", total);
                logDataMap.put("Rejected", failed);
                logDataMap.put("Module", Constants.Acc_UOM_Schema_ModuleId);
                logDataMap.put("ImportDate", new Date());
                logDataMap.put("User", userId);
                logDataMap.put("Company", companyid);
                importDao.saveImportLog(logDataMap);

                String tableName = importDao.getTableName(fileName);
                importDao.removeFileTable(tableName); // Remove table after importing all records

                txnManager.commit(lstatus);
            } catch (Exception ex) {
                txnManager.rollback(lstatus);
                Logger.getLogger(accUomController.class.getName()).log(Level.SEVERE, null, ex);
            }

            try {
                returnObj.put("success", issuccess);
                returnObj.put("msg", msg);
                returnObj.put("totalrecords", total);
                returnObj.put("successrecords", total - failed);
                returnObj.put("failedrecords", failed);
                returnObj.put("filename", getActualFileName(fileName));
            } catch (JSONException ex) {
                Logger.getLogger(accUomController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        return returnObj;
    }
    
    private UOMschemaType getUOMschemaTypeByName(String uomSchemaTypeName, String companyID) throws AccountingException {
        UOMschemaType uomSchemaType = null;
        try {
            if (!StringUtil.isNullOrEmpty(uomSchemaTypeName) && !StringUtil.isNullOrEmpty(companyID)) {
                KwlReturnObject retObj = accUomObj.getUOMschemaTypeByName(uomSchemaTypeName, companyID);
                if (retObj != null && !retObj.getEntityList().isEmpty()) {
                    uomSchemaType = (UOMschemaType) retObj.getEntityList().get(0);
                }
            }
        } catch (Exception ex) {
            Logger.getLogger(accUomController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            throw new AccountingException("System Failure while fetching UOM Schema");
        }
        return uomSchemaType;
    }
    
    private UnitOfMeasure getUOMByName(String productUOMName, String companyID) throws AccountingException {
        UnitOfMeasure uom = null;
        try {
            if (!StringUtil.isNullOrEmpty(productUOMName) && !StringUtil.isNullOrEmpty(companyID)) {
                HashMap<String, Object> filterParams = new HashMap<String, Object>();
                filterParams.put("uomname", productUOMName);
                filterParams.put("companyid", companyID);
                KwlReturnObject retObj = accUomObj.getUnitOfMeasure(filterParams);
                if (retObj != null && !retObj.getEntityList().isEmpty()) {
                    uom = (UnitOfMeasure) retObj.getEntityList().get(0);
                }
            }
        } catch (Exception ex) {
            Logger.getLogger(accUomController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            throw new AccountingException("System Failure while fetching Unit of Measure");
        }
        return uom;
    }
    
    public ModelAndView getUnitOfMeasureOfProductUOMSchema(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        try {
            HashMap<String, Object> requestParams = AccountingManager.getGlobalParams(request);

            String start = request.getParameter(Constants.start);
            String limit = request.getParameter(Constants.limit);
            if (!StringUtil.isNullOrEmpty(request.getParameter("productid"))) {
                requestParams.put("productid", request.getParameter("productid"));
            }
            if (!StringUtil.isNullOrEmpty(request.getParameter("isSalesModule"))) {
                requestParams.put("isSalesModule", request.getParameter("isSalesModule"));
            }

            KwlReturnObject result = accUomObj.getUnitOfMeasureOfProductUOMSchema(requestParams);
            List list = result.getEntityList();
            int count = result.getRecordTotalCount();

            JSONArray DataJArr = accUomService.getUnitOfMeasureOfProductUOMSchemaJSON(request, list);

            JSONArray pagedJson = DataJArr;
            if (!StringUtil.isNullOrEmpty(start) && !StringUtil.isNullOrEmpty(limit)) {
                pagedJson = StringUtil.getPagedJSON(pagedJson, Integer.parseInt(start), Integer.parseInt(limit));
            }
            jobj.put("data", pagedJson);
            jobj.put("count", DataJArr.length());
            issuccess = true;
        } catch (SessionExpiredException ex) {
            msg = ex.getMessage();
            Logger.getLogger(accUomController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ServiceException ex) {
            msg = ex.getMessage();
            Logger.getLogger(accUomController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            msg = "" + ex.getMessage();
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accUomController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
}
